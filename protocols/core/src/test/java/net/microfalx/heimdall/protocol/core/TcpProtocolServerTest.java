package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.IOUtils;
import net.microfalx.lang.ThreadUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TcpProtocolServerTest {

    private static final int BUFFER_SIZE = 500;
    private final AtomicInteger bytes = new AtomicInteger();
    private TcpProtocolServer server;
    private Socket socket;
    private Random random = ThreadLocalRandom.current();

    private int byteCount;

    @BeforeEach
    void setup() throws IOException {
        server = new TcpProtocolServer();
        server.setPort(40000 + random.nextInt(10000));
        server.setHandler(new HandlerImpl());
        server.listen();
        bytes.set(0);

        socket = new Socket();
        socket.connect(new InetSocketAddress(server.getPort()));
        byteCount = BUFFER_SIZE * ((5000 + random.nextInt(5000)) / BUFFER_SIZE);
    }

    @AfterEach
    void destroy() {
        server.shutdown();
    }

    @Test
    void send() throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        sendBytes(outputStream);
        outputStream.close();
        socket.close();
        await().atMost(ofSeconds(5)).until(() -> bytes.get() >= byteCount);
        assertEquals(byteCount, bytes.get());
    }

    private void sendBytes(OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int batchCount = byteCount / BUFFER_SIZE;
        for (int i = 0; i < batchCount; i++) {
            random.nextBytes(buffer);
            outputStream.write(buffer);
            ThreadUtils.sleepMillis(100 + random.nextInt(100));
        }
    }

    private class HandlerImpl implements ProtocolServerHandler {

        @Override
        public void handle(ProtocolServer server, InetAddress address, InputStream inputStream, OutputStream outputStream) throws IOException {
            bytes.addAndGet(IOUtils.getInputStreamAsBytes(inputStream).length);
        }
    }

}