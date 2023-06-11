package net.microfalx.heimdall.protocol.core;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all clients.
 */
public abstract class ProtocolClient<E extends Event> {

    private String hostName = "localhost";
    private int port = 0;
    private Transport transport = Transport.TCP;

    public String getHostName() {
        return hostName;
    }

    public ProtocolClient<E> setHostName(String hostName) {
        requireNotEmpty(hostName);
        this.hostName = hostName;
        configurationChanged();
        return this;
    }

    public int getPort() {
        return port > 0 ? port : getDefaultPort();
    }

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
        return this;
    }

    /**
     * Sends an event using the protocol supported by the client.
     *
     * @param event the event
     */
    public void send(E event) throws IOException {
        requireNonNull(event);
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

    public enum Transport {
        UDP,
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
