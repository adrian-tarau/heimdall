package net.microfalx.heimdall.rest.api;

/**
 * Various constants for Rest service.
 */
public interface RestConstants {

    // various attributes used to split resources
    String SCRIPT_ATTR = "script";
    String LOG_ATTR = "log";
    String REPORT_ATTR = "report";

    // various attributes used to inject into the simulation context
    String VIRTUAL_USERS_ATTR = "vus";
    String DURATION_ATTR = "duration";
    String ITERATIONS_ATTR = "iterations";

}
