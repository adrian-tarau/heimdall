package net.microfalx.heimdall.protocol.gelf;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static net.microfalx.lang.ExceptionUtils.getStackTrace;
import static net.microfalx.lang.TextUtils.insertSpaces;

public class GelfClient extends ProtocolClient<GelfMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GelfClient.class);

    private MdcGelfMessageAssembler assembler;

    @Override
    protected void doSend(GelfMessage event) throws IOException {
        GelfSender sender = createGelfSender();
        assembler.setFacility(event.getFacility().label());
        biz.paluch.logging.gelf.intern.GelfMessage gelfMessage = assembler.createGelfMessage(new LogEventImpl(event));
        if (event.getThrowable() != null) {
            gelfMessage.setFullMessage(event.getBodyAsString() + ", stacktrace:\n"
                    + insertSpaces(getStackTrace(event.getThrowable()), 2, false, false, true));
        }
        for (Map.Entry<String, Object> attribute : event.getAttributes().entrySet()) {
            gelfMessage.addField(attribute.getKey(), ObjectUtils.toString(attribute));
        }
        sender.sendMessage(gelfMessage);
        sender.close();
    }

    @Override
    protected int getDefaultPort() {
        return getTransport() == Transport.TCP ? 12200 : 12201;
    }

    private void addAdditionalFields(MdcGelfMessageAssembler assembler) {
        assembler.addField(new StaticMessageField("Application", "Heimdall"));
        try {
            assembler.addField(new StaticMessageField("Server", InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            // ignore this
        }
    }

    private void createMessageAssembler() throws IOException {
        if (assembler != null) return;
        assembler = new MdcGelfMessageAssembler();
        addAdditionalFields(assembler);
        assembler.setHost(getTransport().name().toLowerCase() + ":" + getHostName());
        assembler.setPort(getPort());
        assembler.setFacility("LOCAL1");
        assembler.setFilterStackTrace(true);
        assembler.setIncludeLocation(true);
        assembler.setOriginHost(InetAddress.getLocalHost().getHostAddress());
        assembler.setTimestampPattern("yyyy-MM-dd HH:mm:ss,SSS");
    }

    protected GelfSender createGelfSender() throws IOException {
        createMessageAssembler();
        return GelfSenderFactory.createSender(assembler, new ErrorReporterImpl(), Collections.<String, Object>emptyMap());
    }

    private static class LogEventImpl implements LogEvent {

        private final GelfMessage message;

        public LogEventImpl(GelfMessage message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            try {
                return message.getBody().getResource().loadAsString();
            } catch (IOException e) {
                return ExceptionUtils.throwException(e);
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
