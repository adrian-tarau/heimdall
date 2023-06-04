package net.microfalx.heimdall.protocol.core;

/**
 * Holds the severity.
 */
public enum Severity {

    /**
     * A severity for very detailed information.
     */
    TRACE(0),

    /**
     * A severity for detailed information.
     */
    DEBUG(1),

    /**
     * A severity for basic information.
     */
    INFO(2),

    /**
     * A severity which indicates a possible issue.
     */
    WARN(3),

    /**
     * A severity which indicates an issue.
     */
    ERROR(4),

    /**
     * A severity which indicates critical issue.
     */
    CRITICAL(5);

    private int level;

    Severity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
