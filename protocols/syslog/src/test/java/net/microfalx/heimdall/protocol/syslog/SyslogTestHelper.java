package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.cloudbees.syslog.sender.TcpSyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class SyslogTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    private String protocol = "tcp";
    private final SyslogConfiguration configuration;
    private SyslogMessageSender sender;

    SyslogTestHelper(SyslogConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
        this.sender = createSyslogSender();
    }

    int getNextPort() {
        return START_PORT + ThreadLocalRandom.current().nextInt(PORT_RANGE);
    }

    void sendLogs() throws IOException {
        send(sender, "Test 1", Severity.INFORMATIONAL);
        //send(client, "Test 2", Severity.WARNING);
        // send(client, "Test 3", Severity.ERROR);
        // send(client, "Test 4", Severity.ALERT);
        // send(client, "Test 5", Severity.CRITICAL);
    }

    void send(SyslogMessageSender client, String message, Severity severity) throws IOException {
        com.cloudbees.syslog.SyslogMessage _message = createMessage(message, severity);
        client.sendMessage(_message);
    }

    private com.cloudbees.syslog.SyslogMessage createMessage(String message, Severity severity) {
        com.cloudbees.syslog.SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setAppName("Heimdall");
        syslogMessage.setFacility(Facility.LOCAL1);
        syslogMessage.setSeverity(severity);
        syslogMessage.setHostname("myhost");
        syslogMessage.setProcId("test");
        //syslogMessage.setMsgId(Integer.toString(IDENTIFIER.getAndIncrement()));
        syslogMessage.withMsg(message);
        syslogMessage.setTimestamp(new Date());
        return syslogMessage;
    }

    private SyslogMessageSender createSyslogSender() {
        AbstractSyslogMessageSender messageSender;
        if ("udp".equalsIgnoreCase(protocol)) {
            UdpSyslogMessageSender udpMessageSender = new UdpSyslogMessageSender();
            udpMessageSender.setSyslogServerPort(configuration.getUdpPort());
            messageSender = udpMessageSender;
        } else {
            TcpSyslogMessageSender tcpMessageSender = new TcpSyslogMessageSender();
            tcpMessageSender.setSyslogServerPort(configuration.getTcpPort());
            messageSender = tcpMessageSender;
        }
        messageSender.setSyslogServerHostname("localhost");
        messageSender.setDefaultMessageHostname("localhost");
        messageSender.setMessageFormat(MessageFormat.RFC_5424);
        return messageSender;
    }


}
