package net.microfalx.heimdall.infrastructure.api;

/**
 * A collection of constants for infrastructure.
 */
public interface InfrastructureConstants {

    // URI Variables
    String BASE_URI = "base_uri";
    String APP_URI = "app_uri";
    String API_URI = "api_uri";
    String REST_API_URI = "rest_api_uri";
    // Host Variables
    String BASE_HOST = "base_host";
    String APP_HOST = "app_host";
    String API_HOST = "api_host";
    String REST_API_HOST = "rest_api_host";
    // Host Variables
    String BASE_PORT = "base_port";
    String APP_PORT = "app_port";
    String API_PORT = "api_port";
    String REST_API_PORT = "rest_api_port";
    // Path Variables
    String BASE_PATH = "base_path";
    String APP_PATH = "app_path";
    String API_PATH = "api_path";
    String REST_API_PATH = "rest_api_path";

    // Credential Variables
    String USERNAME_VARIABLE = "username";
    String PASSWORD_VARIABLE = "password";
    String BEARER_VARIABLE = "bearer";
    String API_KEY_VARIABLE = "api_key";

    String DATABASE_TAG = "db";
    String K8S_TAG = "k8s";
    String API_TAG = "api";
    String OS_TAG = "os";

    int TREND_CHART_WIDTH = 180;
    int TREND_CHART_HEIGHT = 20;
    String TREND_ZERO_FILL_COLOR = "#404040";
    int WINDOW_SIZE = 30;
}
