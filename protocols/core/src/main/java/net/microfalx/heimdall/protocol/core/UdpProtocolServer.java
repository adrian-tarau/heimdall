package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A generic UDP server.
 */
public class UdpProtocolServer extends ProtocolServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpProtocolServer.class);

    private static final int MAX_PACKET_SIZE = 65535;

    private DatagramSocket serverSocket;

    public UdpProtocolServer() {
        setTransport(Transport.UDP);
    }

    @Override
    protected void doListen() throws IOException {
        if (StringUtils.isEmpty(getHostname())) {
            serverSocket = new DatagramSocket(getPort());
        } else {
            serverSocket = new DatagramSocket(getPort(), InetAddress.getByName(getHostname()));
        }
        getExecutor().execute(this::handleClient);
    }

    @Override
    protected void doShutdown() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }

    private void handleClient() {
        while (!serverSocket.isClosed()) {
            try {
                byte[] buffer = new byte[MAX_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                getExecutor().submit(new ClientWorker(packet));
            } catch (SocketException e) {
                if (!serverSocket.isClosed()) {
                    LOGGER.error("Failed to process client connection", e);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to accept client connection", e);
            }
        }
    }

    private class ClientWorker implements Runnable {

        private final DatagramPacket packet;

        ClientWorker(DatagramPacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                getHandler().handle(UdpProtocolServer.this, packet.getAddress(), inputStream, outputStream);
                if (outputStream.size() > 0) {
                    byte[] response = outputStream.toByteArray();
                    serverSocket.send(new DatagramPacket(response, response.length));
                }
            } catch (SocketException e) {
                LOGGER.error("Socket failure while handling request from " + packet.getAddress(), e);
            } catch (IOException e) {
                LOGGER.error("I/O failure while handling request from " + packet.getAddress(), e);
            } catch (Throwable e) {
                LOGGER.error("Internal error while handling request from " + packet.getAddress(), e);
            }
        }
    }
}
