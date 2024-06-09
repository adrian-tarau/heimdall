package net.microfalx.heimdall.infrastructure.api;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * An interface which carries the result of a ping operation.
 */
public interface Ping {

    /**
     * The service which is pinged.
     *
     * @return a non-null instance
     */
    Service getService();

    /**
     * Return the server where the pinged service runs.
     *
     * @return a non-null instance
     */
    Server getServer();

    /**
     * Returns the time when the ping was started.
     *
     * @return a non-null instance
     */
    ZonedDateTime getStartedAt();

    /**
     * Returns the time when the ping was ended.
     *
     * @return a non-null instance
     */
    ZonedDateTime getEndedAt();

    /**
     * Returns the duration of the ping
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the status of the ping.
     *
     * @return a non-null instance
     */
    Status getStatus();

    /**
     * Returns an error message.
     * <p>
     * The error message is present only when the ping fails.
     *
     * @return the error message, null if there is no error
     */
    String getErrorMessage();

    /**
     * An enum which carries the ping status.
     */
    enum Status {

        /**
         * The ping is successful.
         */
        SUCCESS,

        /**
         * The ping failed.
         */
        FAILURE,

        /**
         * The ping failed with a timeout.
         */
        TIMEOUT
    }

}
