package net.microfalx.heimdall.infrastructure.api;

import net.microfalx.lang.annotation.Name;

import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An enum for the status of an environment element (mostly services).
 */
public enum Status {

    /**
     * The health check failed at network layer.
     */
    @Name("L3/CON")
    L3CON(0, true, false),

    /**
     * The health check failed at transport layer ("Connection refused" type of errors).
     */
    @Name("L4/CON")
    L4CON(10, true, false),

    /**
     * The health check failed at transport layer due to a timeout.
     */
    @Name("L4/TOUT")
    L4TOUT(20, true, true),

    /**
     * The health check failed at application layer due to a timeout.
     */
    @Name("L7/TOUT")
    L7TOUT(30, true, true),

    /**
     * The health check failed at application layer due to an <code>invalid response</code> (4XX for HTTP).
     */
    @Name("L7/RSP")
    L7RSP(70, true, false),

    /**
     * The health check failed at application layer due to an <code>response error</code> (5XX for HTTP).
     */
    @Name("L7/STS")
    L7STS(70, true, false),

    /**
     * The health check failed at application layer due to an <code>access denied response</code> (401 or 403 for HTTP).
     */
    @Name("L7/DEN")
    L7DEN(75, false, false),

    /**
     * The health check passed at network layer (ICMP).
     */
    @Name("L3/OK")
    L3OK(80, false, false),

    /**
     * The health check passed at transport layer (TCP/UDP).
     */
    @Name("L4/OK")
    L4OK(90, false, false),

    /**
     * The health check passed at application layer.
     */
    @Name("L4/OK")
    L7OK(100, false, false),

    /**
     * The health check was not executed
     */
    @Name("N/A")
    NA(200, false, false);

    private final int priority;
    private final boolean timeout;
    private final boolean failure;

    Status(int priority, boolean failure, boolean timeout) {
        this.priority = priority;
        this.failure = failure;
        this.timeout = timeout;
    }

    /**
     * Returns whether this status comes before another status regarding the severity of the status.
     *
     * @param status the other status
     * @return {@code true} if this status comes before, {@code false} otherwise
     */
    public boolean isBefore(Status status) {
        requireNonNull(status);
        return priority < status.priority;
    }

    /**
     * Returns whether the status indicate a failure due to a timeout.
     * <p>
     * A timeout only applies to a status which represents a failure.
     *
     * @return {@code true} if a timeout, {@code false} otherwise
     */
    public boolean isTimeout() {
        return timeout;
    }

    /**
     * Returns whether the status indicates a failure.
     *
     * @return {@code true} if a failure, {@code false} otherwise
     */
    public boolean isFailure() {
        return failure;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Status.class.getSimpleName() + "[", "]")
                .add("priority=" + priority)
                .add("name=" + name())
                .toString();
    }
}
