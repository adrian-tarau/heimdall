package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A generic TCP/IP server.
 */
public class TcpProtocolServer extends ProtocolServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProtocolServer.class);

    private ServerSocket serverSocket;

    public TcpProtocolServer() {
        setTransport(Transport.TCP);
    }

    @Override
    protected void doListen() throws IOException {
        if (StringUtils.isEmpty(getHostname())) {
            serverSocket = new ServerSocket(getPort());
        } else {
            serverSocket = new ServerSocket(getPort(), 50, InetAddress.getByName(getHostname()));
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
                Socket clientSocket = serverSocket.accept();
                getExecutor().submit(new ClientWorker(clientSocket));
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

        private final Socket accept;

        ClientWorker(Socket accept) {
            this.accept = accept;
        }

        @Override
        public void run() {
            try {
                getHandler().handle(TcpProtocolServer.this, accept.getInetAddress(),
                        createBufferedInputStream(accept.getInputStream()), accept.getOutputStream());
            } catch (SocketException e) {
                LOGGER.error("Socket failure while handling request from " + accept.getInetAddress(), e);
            } catch (IOException e) {
                LOGGER.error("I/O failure while handling request from " + accept.getInetAddress(), e);
            } catch (Throwable e) {
                LOGGER.error("Internal error while handling request from " + accept.getInetAddress(), e);
            }
            try {
                accept.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close socket for " + accept.getInetAddress());
            }
        }
    }
}
