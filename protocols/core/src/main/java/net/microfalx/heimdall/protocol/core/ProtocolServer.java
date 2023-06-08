package net.microfalx.heimdall.protocol.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all protocol servers.
 */
public abstract class ProtocolServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolServer.class);

    private Transport transport = Transport.TCP;
    private String hostname;
    private int port;
    private ProtocolServerHandler handler;
    private SchedulingTaskExecutor executor;

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
    public SchedulingTaskExecutor getExecutor() {
        return executor;
    }

    /**
     * Changes the executor used by this server.
     *
     * @param executor the executor
     * @return self
     */
    public ProtocolServer setExecutor(SchedulingTaskExecutor executor) {
        requireNonNull(executor);
        this.executor = executor;
        return this;
    }

    /**
     * Starts the server.
     */
    public void listen() {
        if (handler == null) throw new ProtocolException("A handler is required to start the server");
        initExecutor();
        LOGGER.info("Listen on {} ({})", port, describeHostname());
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
        LOGGER.info("Stops the server on {} ({})", port, describeHostname());
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

    private void initExecutor() {
        if (this.executor != null) return;
        LOGGER.info("Start internal thread pool");
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(5);
        threadPool.setMaxPoolSize(10);
        threadPool.setQueueCapacity(500);
        threadPool.setThreadNamePrefix(getClass().getSimpleName().toLowerCase());
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.setAllowCoreThreadTimeOut(true);
        threadPool.setAwaitTerminationSeconds(5);
        threadPool.initialize();

        this.executor = threadPool;
    }

    private String describeHostname() {
        return hostname == null ? "<any>" : hostname;
    }

    public enum Transport {
        UDP,
        TCP
    }
}
