package net.microfalx.heimdall.infrastructure.api;

/**
 * A collection of constants for infrastructure.
 */
public interface InfrastructureConstants {

    // Variables
    String USERNAME_VARIABLE = "username";
    String PASSWORD_VARIABLE = "password";
    String BEARER_VARIABLE = "bearer";
    String API_KEY_VARIABLE = "api_key";

    String AUTO_TAG = "auto";
    String DATABASE_TAG = "db";
    String K8S_TAG = "k8s";
    String API_TAG = "api";
    String OS_TAG = "os";

    int TREND_CHART_WIDTH = 180;
    int TREND_CHART_HEIGHT = 20;
    String TREND_ZERO_FILL_COLOR = "#404040";
    int WINDOW_SIZE = 30;
}
