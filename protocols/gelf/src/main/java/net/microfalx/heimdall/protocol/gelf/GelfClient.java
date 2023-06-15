package net.microfalx.heimdall.protocol.gelf;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

public class
GelfClient extends ProtocolClient<GelfMessage> implements ErrorReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GelfClient.class);

    private MdcGelfMessageAssembler assembler;
    private String message;
    private Throwable throwable;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void reportError(String message, Exception e) {
        LOGGER.error(message, e);
    }

    @Override
    protected void doSend(GelfMessage event) throws IOException {
        GelfSender sender = createGelfSender();
        biz.paluch.logging.gelf.intern.GelfMessage gelfMessage = assembler.createGelfMessage(new LogEventImpl());
        sender.sendMessage(gelfMessage);
        sender.close();
    }

    @Override
    protected int getDefaultPort() {
        return getTransport() == Transport.TCP ? 12200 : 12201;
    }

    private void addAdditionalFields(MdcGelfMessageAssembler assembler) {
        assembler.addField(new StaticMessageField("Process", "Heimdall Test"));
        assembler.addField(new StaticMessageField("Server", "Self"));
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

    private class LogEventImpl implements LogEvent {

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Object[] getParameters() {
            return new Object[0];
        }

        @Override
        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public long getLogTimestamp() {
            return System.currentTimeMillis();
        }

        @Override
        public String getSyslogLevel() {
            return "2";
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

    private class ErrorReporterImpl implements ErrorReporter {
        @Override
        public void reportError(String message, Exception e) {

        }
    }
}
