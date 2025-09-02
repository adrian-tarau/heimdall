package net.microfalx.heimdall.protocol.gelf;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;

public class GelfClient extends ProtocolClient<GelfEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GelfClient.class);

    private MdcGelfMessageAssembler assembler;

    @Override
    protected void doSend(GelfEvent event) throws IOException {
        createMessageAssembler(event);
        GelfSender sender = createGelfSender();
        biz.paluch.logging.gelf.intern.GelfMessage gelfMessage = assembler.createGelfMessage(new LogEventImpl(event));
        for (Attribute attribute : event) {
            gelfMessage.addField(attribute.getName(), attribute.asString());
        }
        sender.sendMessage(gelfMessage);
        sender.close();
    }

    @Override
    protected int getDefaultPort() {
        return getTransport() == Transport.TCP ? 12200 : 12201;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.GELF;
    }

    private void addAdditionalFields(MdcGelfMessageAssembler assembler, GelfEvent event) {
        String application = StringUtils.defaultIfEmpty(event.getApplication(), "Heimdall");
        assembler.addField(new StaticMessageField("Application", application));
        if (StringUtils.isNotEmpty(event.getLogger())) {
            assembler.addField(new StaticMessageField("LoggerName", event.getLogger()));
        }
        try {
            assembler.addField(new StaticMessageField("Server", InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException e) {
            // ignore this
        }
    }

    private void createMessageAssembler(GelfEvent event) throws IOException {
        assembler = new MdcGelfMessageAssembler();
        assembler.setFacility(event.getFacility().label());
        assembler.setHost(getTransport().name().toLowerCase() + ":" + getHostName());
        assembler.setPort(getPort());
        assembler.setOriginHost(event.getSource().getName());
        assembler.setExtractStackTrace(true);
        assembler.setFilterStackTrace(true);
        assembler.setIncludeLocation(true);
        assembler.setTimestampPattern("yyyy-MM-dd HH:mm:ss,SSS");
        addAdditionalFields(assembler, event);
    }

    protected GelfSender createGelfSender() throws IOException {
        return GelfSenderFactory.createSender(assembler, new ErrorReporterImpl(), Collections.<String, Object>emptyMap());
    }

    private static class LogEventImpl implements LogEvent {

        private final GelfEvent message;

        public LogEventImpl(GelfEvent message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            try {
                return message.getBody().getResource().loadAsString();
            } catch (IOException e) {
                return rethrowExceptionAndReturn(e);
            }
        }

        @Override
        public Object[] getParameters() {
            return new Object[0];
        }

        @Override
        public Throwable getThrowable() {
            return message.getThrowable();
        }

        @Override
        public long getLogTimestamp() {
            return System.currentTimeMillis();
        }

        @Override
        public String getSyslogLevel() {
            return Integer.toString(message.getGelfSeverity().numericalCode());
        }

        @Override
        public Values getValues(MessageField field) {
            return null;
        }

        @Override
        public String getMdcValue(String mdcName) {
            return null;
        }

        @Override
        public Set<String> getMdcNames() {
            return Collections.emptySet();
        }
    }

    private static class ErrorReporterImpl implements ErrorReporter {

        @Override
        public void reportError(String message, Exception e) {
            LOGGER.error(message, e);
        }
    }
}
