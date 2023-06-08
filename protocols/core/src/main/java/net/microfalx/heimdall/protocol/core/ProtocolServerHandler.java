package net.microfalx.heimdall.protocol.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * An interface used to handle client requests.
 */
public interface ProtocolServerHandler {

    /**
     * Invoked to handle the client request.
     *
     * @param server       the server which received the request
     * @param address      the client address
     * @param inputStream  the input stream
     * @param outputStream the output stream
     * @throws IOException if there is an I/O error
     */
    void handle(ProtocolServer server, InetAddress address, InputStream inputStream, OutputStream outputStream) throws IOException;
}
