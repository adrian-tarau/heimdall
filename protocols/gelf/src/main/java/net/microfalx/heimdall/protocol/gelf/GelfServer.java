package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.lang.IOUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_NAME_LENGTH;
import static net.microfalx.lang.IOUtils.appendStream;
import static net.microfalx.lang.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Component
public class GelfServer implements InitializingBean, ProtocolServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    @Autowired
    private GelfConfiguration configuration;

    @Autowired
    private GelfService gelfService;

    private TcpProtocolServer tcpServer;
    private UdpProtocolServer udpServer;

    private final Map<Long, SortedSet<GelfChunk>> chunks = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    public void initialize() {
        initializeTcpServer();
        initializeUdpServer();
    }

    @PreDestroy
    protected void destroy() {
        if (tcpServer != null) tcpServer.shutdown();
        if (udpServer != null) udpServer.shutdown();
    }

    private void initializeTcpServer() {
        tcpServer = new TcpProtocolServer();
        tcpServer.setPort(configuration.getTcpPort());
        tcpServer.setExecutor(gelfService.getTaskExecutor());
        tcpServer.setHandler(this);
        tcpServer.listen();
    }

    private void initializeUdpServer() {
        udpServer = new UdpProtocolServer();
        udpServer.setPort(configuration.getUdpPort());
        udpServer.setExecutor(gelfService.getTaskExecutor());
        udpServer.setHandler(this);
        udpServer.listen();
    }

    @Override
    public void handle(ProtocolServer server, InetAddress address, InputStream inputStream, OutputStream outputStream) throws IOException {
        if (server.getTransport() == ProtocolServer.Transport.UDP) {
            handleUdp(address, inputStream);
        } else {
            doHandle(address, inputStream);
        }
    }

    private void handleUdp(InetAddress address, InputStream inputStream) throws IOException {
        inputStream = decompress(inputStream);
        inputStream.mark(20);
        DataInput input = new DataInputStream(inputStream);
        short chunkMarker = input.readShort();
        if (chunkMarker != GelfChunk.MARKER) {
            inputStream.reset();
            doHandle(address, inputStream);
        } else {
            handleUdpChunk(address, inputStream);
        }
    }

    private void handleUdpChunk(InetAddress address, InputStream inputStream) throws IOException {
        DataInput input = new DataInputStream(inputStream);
        long messageID = input.readLong();
        SortedSet<GelfChunk> messageChunks = chunks.computeIfAbsent(messageID, key -> new ConcurrentSkipListSet<>());
        byte index = input.readByte();
        byte count = input.readByte();
        Resource chunkResource = MemoryResource.create(IOUtils.getInputStreamAsBytes(inputStream));
        GelfChunk chunk = new GelfChunk(index, chunkResource);
        messageChunks.add(chunk);
        if (messageChunks.size() == count) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Set<GelfChunk> chunksForMessage = chunks.get(messageID);
            chunks.remove(messageID);
            Collection<Resource> resourceStream = chunksForMessage.stream().map(GelfChunk::getResource).toList();
            for (Resource resource : resourceStream) {
                appendStream(outputStream, resource.getInputStream(true));
            }
            doHandle(address, decompress(new ByteArrayInputStream(outputStream.toByteArray())));
        }
    }


    private InputStream decompress(InputStream inputStream) {
        try {
            String detect = CompressorStreamFactory.detect(inputStream);
            inputStream = new CompressorStreamFactory().createCompressorInputStream(detect, inputStream);
            return new BufferedInputStream(inputStream, ProtocolServer.BUFFER_SIZE);
        } catch (CompressorException e) {
            return inputStream;
        }
    }

    private void doHandle(InetAddress address, InputStream inputStream) throws IOException {
        JsonNode jsonNode = readJson(inputStream);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received GELF event:\n{}", jsonNode.toPrettyString());
        String shortMessage = defaultIfEmpty(getField(jsonNode, "short_message"), NA_STRING);
        String fullMessage = getRequiredField(jsonNode, "full_message");
        String host = net.microfalx.lang.StringUtils.defaultIfNull(getField(jsonNode, "host"), "0.0.0.0");
        GelfEvent message = new GelfEvent();
        message.setFacility(Facility.fromLabel(getRequiredField(jsonNode, "facility")));
        message.setName(abbreviate(removeLineBreaks(shortMessage), MAX_NAME_LENGTH));
        message.setReceivedAt(ZonedDateTime.now());
        message.setSource(Address.create(Address.Type.HOSTNAME, host));
        message.addPart(Body.create(shortMessage));
        if (!shortMessage.equals(fullMessage)) message.addPart(Body.create(fullMessage));
        message.setCreatedAt(createTimeStamp(jsonNode));
        message.setSentAt(createTimeStamp(jsonNode));
        message.setGelfSeverity(Severity.fromNumericalCode(getRequiredIntField(jsonNode, "level")));
        addAllJsonFields(message, jsonNode);
        gelfService.accept(message);
    }

    private void addAllJsonFields(GelfEvent gelfEvent, JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().startsWith("_")) {
                gelfEvent.add(field.getKey().substring(1), field.getValue().textValue());
            }
        }
    }

    private ZonedDateTime createTimeStamp(JsonNode jsonNode) {
        long seconds = getRequiredLongField(jsonNode, "timestamp");
        Instant instant = Instant.ofEpochSecond(seconds);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private String getField(JsonNode jsonNode, String field) {
        JsonNode fieldNode = jsonNode.findValue(field);
        if (fieldNode == null) throw new ProtocolException("A required field (" + field + ") does not exist");
        return fieldNode != null ? fieldNode.asText() : null;
    }

    private String getRequiredField(JsonNode jsonNode, String field) {
        JsonNode fieldNode = jsonNode.findValue(field);
        if (fieldNode == null) throw new ProtocolException("A required field (" + field + ") does not exist");
        return fieldNode.asText();
    }

    private long getRequiredLongField(JsonNode jsonNode, String field) {
        JsonNode fieldNode = jsonNode.findValue(field);
        if (fieldNode == null) throw new ProtocolException("A required field (" + field + ") does not exist");
        return fieldNode.asLong();
    }

    private int getRequiredIntField(JsonNode jsonNode, String field) {
        JsonNode fieldNode = jsonNode.findValue(field);
        if (fieldNode == null) throw new ProtocolException("A required field (" + field + ") does not exist");
        return fieldNode.asInt();
    }

    /**
     * Decodes the GELF json message from the stream.
     *
     * @param inputStream the input stream
     * @return the JSON tree
     */
    private JsonNode readJson(InputStream inputStream) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(inputStream);
        } catch (IOException e) {
            throw new ProtocolException("Failed to extract Gelf JSON payload", e);
        }
    }
}
