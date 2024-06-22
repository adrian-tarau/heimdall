package net.microfalx.heimdall.infrastructure.api;

import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An enum for the status of an environment element (mostly services).
 */
public enum Status {

    /**
     * The health check failed at network layer.
     */
    L3CON(0, true),

    /**
     * The health check failed at transport layer ("Connection refused" type of errors).
     */
    L4CON(10, true),

    /**
     * The health check failed at transport layer due to a timeout.
     */
    L4TOUT(20, true),

    /**
     * The health check failed at application layer due to a timeout.
     */
    L7TOUT(30, true),

    /**
     * The health check failed at application layer due to an <code>invalid response</code> (4XX for HTTP).
     */
    L7RSP(70, true),

    /**
     * The health check failed at application layer due to an <code>response error</code> (5XX for HTTP).
     */
    L7STS(70, true),

    /**
     * The health check passed at network layer (ICMP).
     */
    L3OK(80, false),

    /**
     * The health check passed at transport layer (TCP/UDP).
     */
    L4OK(90, false),

    /**
     * The health check passed at application layer.
     */
    L7OK(100, false),

    /**
     * The health check was not executed
     */
    NA(200, false);

    private final int priority;
    private final boolean failure;

    Status(int priority, boolean failure) {
        this.priority = priority;
        this.failure = failure;
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
