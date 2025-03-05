package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all protocol servers.
 */
public abstract class ProtocolServer {

    private volatile Logger LOGGER;

    public static final int BUFFER_SIZE = 4 * 1024;

    private Transport transport = Transport.TCP;
    private String hostname;
    private int port;
    private ProtocolServerHandler handler;
    private ThreadPool threadPool;

    /**
     * Returns the transport protocol used by this server.
     *
     * @return a non-null instance
     */
    public Transport getTransport() {
        return transport;
    }

    /**
     * Changes the transport.
     *
     * @param transport the transport
     */
    protected void setTransport(Transport transport) {
        requireNonNull(transport);
        this.transport = transport;
    }

    /**
     * Returns the interface where to listen.
     *
     * @return the hostname, null to listen on all interfaces
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Changes the hostname/interface.
     *
     * @param hostname the hostname
     * @return self
     */
    public ProtocolServer setHostname(String hostname) {
        requireNotEmpty(hostname);
        this.hostname = hostname;
        return this;
    }

    /**
     * Returns the port where the server will listen.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Changes the port where the server will listen.
     *
     * @param port the port
     * @return self
     */
    public ProtocolServer setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Returns the client handler.
     *
     * @return the handler, null if not set
     */
    public ProtocolServerHandler getHandler() {
        return handler;
    }

    /**
     * Changes the client handler.
     *
     * @param handler the handler
     */
    public ProtocolServer setHandler(ProtocolServerHandler handler) {
        requireNonNull(handler);
        this.handler = handler;
        return this;
    }

    /**
     * Returns the executor used by this server.
     *
     * @return the executor, null if not set
     */
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * Changes the executor used by this server.
     *
     * @param threadPool the executor
     * @return self
     */
    public ProtocolServer setThreadPool(ThreadPool threadPool) {
        requireNonNull(threadPool);
        this.threadPool = threadPool;
        return this;
    }

    /**
     * Starts the server.
     */
    public void listen() {
        if (handler == null) throw new ProtocolException("A handler is required to start the server");
        initExecutor();
        getLogger().info("Listen on {} for {}", port, getTransport().name());
        try {
            doListen();
        } catch (IOException e) {
            throw new ProtocolException("Failed to start server on " + port + " (" + describeHostname() + ")", e);
        }
    }

    /**
     * Stops the server and releases all resources.
     */
    public void shutdown() {
        getLogger().info("Shutdown at {} for {}", port, getTransport().name());
        try {
            doShutdown();
        } catch (IOException e) {
            LOGGER.info("Failed to destroy server on {} ({})", port, describeHostname());
        }
    }

    /**
     * Subclasses will start the server.
     */
    protected abstract void doListen() throws IOException;

    /**
     * Subclasses will stop the server.
     */
    protected abstract void doShutdown() throws IOException;

    /**
     * Creates a buffered input stream from a network stream.
     *
     * @param inputStream the network stream
     * @return a non-null instance
     */
    protected final InputStream createBufferedInputStream(InputStream inputStream) {
        return new BufferedInputStream(inputStream, BUFFER_SIZE);
    }

    /**
     * Returns a logger using the subclass name.
     *
     * @return a non-null instance
     */
    protected final Logger getLogger() {
        if (LOGGER == null) LOGGER = LoggerFactory.getLogger(getClass());
        return LOGGER;
    }

    private void initExecutor() {
        if (this.threadPool != null) return;
        getLogger().info("Start internal thread pool");
        this.threadPool = ThreadPoolFactory.create("Protocol").create();
    }

    private String describeHostname() {
        return hostname == null ? "*" : hostname;
    }

    public enum Transport {
        UDP,
        TCP
    }
}
