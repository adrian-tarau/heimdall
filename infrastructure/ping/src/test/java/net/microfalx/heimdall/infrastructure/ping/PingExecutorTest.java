package net.microfalx.heimdall.infrastructure.ping;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.ThreadUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PingExecutorTest {

    private PingExecutor pingExecutor;
    private Ping ping;
    private Service service;
    private Server server;
    @Mock
    private InfrastructureService infrastructureService;
    @Mock
    private PingRepository pingRepository;
    @Mock
    PingResultRepository pingResultRepository;
    @InjectMocks
    private PingPersistence persistence;

    private HttpServer httpServer;
    private final Random random = ThreadLocalRandom.current();

    @BeforeEach
    void setup() {
        server = new Server.Builder().hostname("localhost").build();
        service = Service.create(Service.Type.ICMP);
        ping = new Ping();
    }

    @AfterEach
    void destroy() {
        if (httpServer != null) httpServer.stop(0);
    }

    @Test
    void executePingOverICMPWithIcmpEnabled() {
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingOverICMPWithIcmpDisable() {
        server = new Server.Builder().hostname("localhost").icmp(false).build();
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.CANCEL, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingTCPWithSuccess() throws IOException {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.TCP).port(port).build();
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(service.getPort()));
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        serverSocket.accept();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingTCPWithFailure() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.TCP).port(port).build();
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndSuccess() {
        service = new Service.Builder().type(Service.Type.HTTP)
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new OkHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndSuccessNoneAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.NONE)
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new OkHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndSuccessBasicAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.BASIC)
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new OkHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndSuccessBearerAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.BEARER)
                .userName("alex").password("alex123").path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new OkHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndSuccessApiKeyAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.API_KEY)
                .apiKey("d73b0d68-191c-445f-a48f-b605c3cadafd").path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new OkHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndTimeOut() {
        service = new Service.Builder().type(Service.Type.HTTP)
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new TimeoutHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.TIMEOUT, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }


    @Test
    void executePingWithHTTPAndTimeOutNoneAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.NONE)
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new TimeoutHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.TIMEOUT, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndTimeOutBasicAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.BASIC).userName("alex")
                .password("alex123").path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new TimeoutHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.TIMEOUT, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndTimeOutBearerAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.BEARER).token("12345")
                .path("/ping/http_service").port(getNextPort()).build();
        createHttpServer(new TimeoutHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.TIMEOUT, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndTimeOutApiKeyAuthentication() {
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.API_KEY)
                .apiKey("d73b0d68-191c-445f-a48f-b605c3cadafd").path("/ping/http_service")
                .port(getNextPort()).build();
        createHttpServer(new TimeoutHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.TIMEOUT, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingWithHTTPAndFailure() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.HTTP)
                .path("/ping/http_service").port(port).build();
        createHttpServer(new FailureHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
        assertEquals(500,result.getErrorCode());
    }

    @Test
    void executePingWithHTTPAndFailureAndNoneAuthentication() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.NONE)
                .path("/ping/http_service").port(port).build();
        createHttpServer(new FailureHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
        assertEquals(500,result.getErrorCode());
    }

    @Test
    void executePingWithHTTPAndFailureBasicAuthentication() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.HTTP)
                .authType(Service.AuthType.BASIC).path("/ping/http_service").port(port).build();
        createHttpServer(new FailureHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
        assertEquals(500,result.getErrorCode());
    }


    @Test
    void executePingWithHTTPAndFailureAndBearerAuthentication() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.HTTP).authType(Service.AuthType.BEARER)
                .path("/ping/http_service").port(port).build();
        createHttpServer(new FailureHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
        assertEquals(500,result.getErrorCode());
    }

    @Test
    void executePingWithHTTPAndFailureAndApiKeyAuthentication() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().type(Service.Type.HTTP)
                .authType(Service.AuthType.API_KEY).path("/ping/http_service").port(port)
                .apiKey("d73b0d68-191c-445f-a48f-b605c3cadafd").build();
        createHttpServer(new FailureHandler());
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
        assertEquals(500,result.getErrorCode());
    }

    private int getNextPort() {
        return 49000 + random.nextInt(10000);
    }

    private void createHttpServer(HttpHandler handler) {
        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), service.getPort()), 100);
            httpServer.createContext("/", handler);
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.start();
        } catch (IOException e) {
            ExceptionUtils.throwException(e);
        }
    }

    static class OkHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] data = "OK".getBytes();
            exchange.sendResponseHeaders(HttpServletResponse.SC_OK, data.length);
            IOUtils.appendStream(exchange.getResponseBody(), new ByteArrayInputStream(data));
        }
    }

    static class FailureHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) {
            throw new RuntimeException("We need to fail");
        }
    }

    static class TimeoutHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ThreadUtils.sleepSeconds(10);
            exchange.getResponseBody().close();
        }
    }

}