package net.microfalx.heimdall.protocol.gelf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

@Service
public class GelfServerService implements ProtocolServerHandler {

    @Autowired
    private GelfConfiguration configuration;

    @Autowired
    private GelfService gelfService;

    private ThreadPoolTaskExecutor executor;
    private TcpProtocolServer tcpServer;
    private UdpProtocolServer udpServer;
    @Autowired
    private AddressRepository addressRepository;

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
        JsonNode jsonNode = readJson(inputStream);
        System.out.println("JSON:" + jsonNode.toPrettyString());
        // in here look at GELF docs: TCP has the JSON in input stream, UDP will have a fragment of the message
        // the server tells you which protocol was used to receive the message
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
