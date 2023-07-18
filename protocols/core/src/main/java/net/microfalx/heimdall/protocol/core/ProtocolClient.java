package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.ExceptionUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.UnknownHostException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all clients.
 */
public abstract class ProtocolClient<E extends Event> {

    private String hostName = "localhost";
    private int port = 0;
    private Transport transport = Transport.UDP;

    /**
     * Returns the hostname used by the client.
     *
     * @return a non-null instance
     */
    public final String getHostName() {
        return hostName;
    }

    /**
     * Returns the {@link InetAddress} used by the client.
     *
     * @return a non-null instance
     */
    public final InetAddress getAddress() {
        try {
            return InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    /**
     * Changes the hostname used by the client.
     *
     * @param hostName the hostname
     * @return self
     */
    public ProtocolClient<E> setHostName(String hostName) {
        requireNotEmpty(hostName);
        this.hostName = hostName;
        configurationChanged();
        return this;
    }

    /**
     * Returns the port used by the client.
     *
     * @return a positive integer
     */
    public int getPort() {
        return port > 0 ? port : getDefaultPort();
    }

    /**
     * Changes the port used by the client.
     *
     * @param port the port
     * @return self
     */
    public ProtocolClient<E> setPort(int port) {
        this.port = port;
        configurationChanged();
        return this;
    }

    /**
     * Returns the transport protocol used by this client.
     *
     * @return a non-null instance
     */
    public Transport getTransport() {
        return transport;
    }

    /**
     * Changes the transport protocol used by this client.
     *
     * @param transport the transport
     * @return self
     */
    public ProtocolClient<E> setTransport(Transport transport) {
        requireNonNull(hostName);
        this.transport = transport;
        configurationChanged();
        return doSetTransport(transport, true);
    }

    /**
     * Sends an event using the protocol supported by the client.
     *
     * @param event the event
     */
    public void send(E event) throws IOException {
        requireNonNull(event);
        if (event.getSource() == null) throw new ProtocolException("A source address is required");
        if (event.getTargets().isEmpty()) throw new ProtocolException("At least one target address is required");
        if (event.getParts().isEmpty()) throw new ProtocolException("At least one part is required");
        doSend(event);
    }

    /**
     * Returns the default port.
     * <p>
     * Each transport protocol has a different port.
     *
     * @return the port
     */
    protected abstract int getDefaultPort();

    /**
     * Subclasses will implement this method to send an event.
     *
     * @param event the event
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doSend(E event) throws IOException;

    /**
     * Invoked when the configuration of the client changed.
     */
    protected void configurationChanged() {
        // empty by default
    }

    protected final ProtocolClient<E> doSetTransport(Transport transport, boolean fireChange) {
        requireNonNull(hostName);
        this.transport = transport;
        if (fireChange) configurationChanged();
        return this;
    }

    /**
     * An enum which decides which transport the client will use
     */
    public enum Transport {

        /**
         * Uses UDP to communicate with the server
         */
        UDP,

        /**
         * Uses TCP to communicate with the server
         */
        TCP
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "ProtocolClient{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                ", transport=" + transport +
                '}';
    }
}
