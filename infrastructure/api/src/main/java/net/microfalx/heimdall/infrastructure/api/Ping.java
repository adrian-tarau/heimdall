package net.microfalx.heimdall.infrastructure.api;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * An interface which carries the result of a ping operation.
 */
public interface Ping extends Identifiable<String>, Nameable {

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
     * Returns the error code.
     *
     * @return {@code the code} if there is an error code,  null otherwise
     */
    Integer getErrorCode();

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
        TIMEOUT,

        /**
         * The ping is canceled
         */
        CANCEL
    }

}
