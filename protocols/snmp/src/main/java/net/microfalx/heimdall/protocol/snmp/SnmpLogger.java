package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogLevel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

import static org.snmp4j.log.LogFactory.SNMP4J_LOG_FACTORY_SYSTEM_PROPERTY;

public class SnmpLogger implements LogAdapter {

    private final String name;
    private final Logger LOGGER;

    private LogLevel logLevel = LogLevel.INFO;

    public SnmpLogger(String name) {
        this.name = name;
        this.LOGGER = LoggerFactory.getLogger(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return LOGGER.isWarnEnabled();
    }

    @Override
    public void debug(Serializable message) {
        LOGGER.debug(ObjectUtils.toString(message));
    }

    @Override
    public void info(CharSequence message) {
        LOGGER.debug(ObjectUtils.toString(message));
    }

    @Override
    public void warn(Serializable message) {
        LOGGER.warn(ObjectUtils.toString(message));
    }

    @Override
    public void error(Serializable message) {
        LOGGER.error(ObjectUtils.toString(message));
    }

    @Override
    public void error(CharSequence message, Throwable throwable) {
        LOGGER.atError().setCause(throwable).log(ObjectUtils.toString(message));
    }

    @Override
    public void fatal(Object message) {
        LOGGER.error(ObjectUtils.toString(message));
    }

    @Override
    public void fatal(CharSequence message, Throwable throwable) {
        LOGGER.atError().setCause(throwable).log(ObjectUtils.toString(message));
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public LogLevel getEffectiveLogLevel() {
        return logLevel;
    }

    @Override
    public Iterator<?> getLogHandler() {
        return Collections.emptyIterator();
    }

    static void init() {
        System.setProperty(SNMP4J_LOG_FACTORY_SYSTEM_PROPERTY, SnmpLogger.Factory.class.getName());
    }

    public static class Factory extends LogFactory {

        @Override
        protected LogAdapter createLogger(Class<?> c) {
            return createLogger(c.getName());
        }

        @Override
        protected LogAdapter createLogger(String className) {
            return new SnmpLogger(className);
        }
    }
}
