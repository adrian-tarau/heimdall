package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.lang.Identifiable;

import java.net.SocketAddress;
import java.util.UUID;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A session for a syslog client.
 */
public class SyslogSession implements Identifiable<String> {

    private final String id = UUID.randomUUID().toString();
    private final SocketAddress socketAddress;

    SyslogSession(SocketAddress socketAddress) {
        requireNonNull(socketAddress);
        this.socketAddress = socketAddress;
    }

    @Override
    public String getId() {
        return id;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    @Override
    public String toString() {
        return "SyslogSession{" +
                "id='" + id + '\'' +
                ", socketAddress=" + socketAddress +
                '}';
    }
}
