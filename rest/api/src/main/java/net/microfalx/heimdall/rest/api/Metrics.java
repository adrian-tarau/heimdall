package net.microfalx.heimdall.rest.api;

import net.microfalx.metrics.Metric;

/**
 * A collection of metrics returned by simulations.
 */
public class Metrics {

    public static final Metric DATA_RECEIVED = Metric.create("data_received");
    public static final Metric DATA_SENT = Metric.create("data_sent");
    public static final Metric ITERATIONS = Metric.create("iterations");
    public static final Metric ITERATION_DURATION = Metric.create("iteration_duration");
    public static final Metric VUS = Metric.create("vus");
    public static final Metric VUS_MAX = Metric.create("vus_max");

    public static final Metric HTTP_REQS = Metric.create("http_reqs");
    public static final Metric HTTP_REQ_DURATION = Metric.create("http_req_duration");
    public static final Metric HTTP_REQ_BLOCKED = Metric.create("http_req_blocked");
    public static final Metric HTTP_REQ_CONNECTING = Metric.create("http_req_connecting");
    public static final Metric HTTP_REQ_TLS_HANDSHAKING = Metric.create("http_req_tls_handshaking");
    public static final Metric HTTP_REQ_SENDING = Metric.create("http_req_sending");
    public static final Metric HTTP_REQ_WAITING = Metric.create("http_req_waiting");
    public static final Metric HTTP_REQ_RECEIVING = Metric.create("http_req_receiving");
    public static final Metric HTTP_REQ_FAILED = Metric.create("http_req_failed");
    public static final Metric HTTP_REQ_FAILED_4XX = Metric.create("http_req_failed_4xx");
    public static final Metric HTTP_REQ_FAILED_5XX = Metric.create("http_req_failed_5xx");
}
