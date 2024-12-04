package net.microfalx.heimdall.rest.api;

import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Vector;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * The output of a scenario.
 * <p>
 * A simulation can have one or more scenarios.
 */
public interface Output extends Identifiable<String>, Nameable {

    /**
     * Returns the scenario which produced this output.
     *
     * @return a non-null instance
     */
    Scenario getScenario();

    /**
     * Returns the environment targeted by a simulation.
     *
     * @return a non-null instance
     */
    Environment getEnvironment();

    /**
     * Returns the simulation used to produce this output.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

    /**
     * Returns the start time of the simulation.
     *
     * @return a non-null instance
     */
    LocalDateTime getStartTime();

    /**
     * Returns the end time of the simulation.
     *
     * @return a non-null instance
     */
    LocalDateTime getEndTime();

    /**
     * Returns the duration of the simulation.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the amount of received data.
     *
     * @return a non-null instance
     */
    Vector getDataReceived();

    /**
     * Returns the amount of data sent.
     *
     * @return a non-null instance
     */
    Vector getDataSent();

    /**
     * Return the aggregate number of times the VUs execute the script.
     *
     * @return a non-null instance
     */
    Vector getIterations();

    /**
     * Return the time to complete one full iteration, including time spent in setup and teardown.
     *
     * @return a non-null instance
     */
    Matrix getIterationDuration();

    /**
     * Return the current number of active virtual users.
     *
     * @return a non-null instance
     */
    Matrix getVus();

    /**
     * Returns the maximum possible number of virtual users.
     *
     * @return a non-null instance
     */
    Matrix getVusMax();

    /**
     * Returns the time spent blocked (waiting for a free TCP connection slot) before initiating the request.
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestBlocked();

    /**
     * Returns the time spent establishing TCP connection to the remote host.
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestConnecting();

    /**
     * Returns the total time for the request. It’s equal to http_req_sending + http_req_waiting + http_req_receiving
     * (i.e. how long did the remote server take to process the request and respond, without the initial
     * DNS lookup/connection times).
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestDuration();

    /**
     * Returns the rate of failed requests.
     *
     * @return a non-null instance
     */
    Vector getHttpRequestFailed();

    /**
     * Returns the time spent receiving response data from the remote host.
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestReceiving();

    /**
     * Returns the time spent sending data to the remote host.
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestSending();

    /**
     * Returns the time spent handshaking TLS session with remote host.
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestTlsHandshaking();

    /**
     * Returns the time spent waiting for response from remote host (a.k.a. “time to first byte”, or “TTFB”).
     *
     * @return a non-null instance
     */
    Matrix getHttpRequestWaiting();

    /**
     * Returns how many total HTTP requests were generated.
     *
     * @return a non-null instance
     */
    Vector getHttpRequests();

    /**
     * The APDEX score for the scenario
     *
     * @return a non-null instance
     */
    float getApdex();
}
