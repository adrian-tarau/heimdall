package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;

@Service
public class GelfServerService implements ProtocolServerHandler {

    @Autowired
    private GelfConfiguration configuration;

    @Autowired
    private GelfService gelfService;

    private ThreadPoolTaskExecutor executor;
    private TcpProtocolServer tcpServer;
    private UdpProtocolServer udpServer;

    @PostConstruct
    protected void initialize() {
        initThreadPool();
        initializeTcpServer();
        initializeUdpServer();
    }

    @PreDestroy
    protected void destroy() {
        if (tcpServer != null) tcpServer.shutdown();
        if (udpServer != null) udpServer.shutdown();
        if (executor != null) executor.destroy();
    }

    private void initializeTcpServer() {
        tcpServer = new TcpProtocolServer();
        tcpServer.setPort(configuration.getTcpPort());
        tcpServer.setExecutor(executor);
        tcpServer.setHandler(this);
        tcpServer.listen();
    }

    private void initializeUdpServer() {
        udpServer = new UdpProtocolServer();
        udpServer.setPort(configuration.getUdpPort());
        udpServer.setExecutor(executor);
        udpServer.setHandler(this);
        udpServer.listen();
    }

    @Override
    public void handle(ProtocolServer server, InetAddress address, InputStream inputStream, OutputStream outputStream) throws IOException {
        System.out.println("Address:" + address);
        // TODO for UDP, the event will come in chunks, you have to accumulate chunks and reassmable the event when las one comes
        JsonNode jsonNode = readJson(inputStream);
        GelfMessage message = new GelfMessage();
        System.out.println("JSON:" + jsonNode.toPrettyString());
        // in here look at GELF docs: TCP has the JSON in input stream, UDP will have a fragment of the message
        // the server tells you which protocol was used to receive the message
        message.setFacility(Facility.fromLabel(getRequiredField(jsonNode, "facility")));
        message.setName("Gelf Message");
        message.setReceivedAt(ZonedDateTime.now());
        message.setSource(Address.create(address.getHostName(), address.getHostAddress()));
        message.addPart(Body.create(message, "shortMessage"));
        message.addPart(Body.create(message, "fullMessage"));
        message.setCreatedAt(createTimeStamp(jsonNode));
        message.setSentAt(createTimeStamp(jsonNode));
        message.setGelfMessageSeverity(Severity.fromLabel(getRequiredField(jsonNode, "level")));
        addAllJsonFields(message, jsonNode);
        gelfService.handle(message);
    }


    private void addAllJsonFields(GelfMessage gelfMessage, JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().startsWith("_")) {
                gelfMessage.addAttribute(field.getKey().substring(1), field.getValue());
            }
        }
    }

    private ZonedDateTime createTimeStamp(JsonNode jsonNode) {
        long seconds = getRequiredLongField(jsonNode, "timestamp");
        Instant instant = Instant.ofEpochSecond(seconds);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
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

    private long getRequiredIntField(JsonNode jsonNode, String field) {
        JsonNode fieldNode = jsonNode.findValue(field);
        if (fieldNode == null) throw new ProtocolException("A required field (" + field + ") does not exist");
        return fieldNode.asInt();
    }

    private void initThreadPool() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("heimdall-gelf");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
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
