package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.cloudbees.syslog.sender.TcpSyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;
import net.microfalx.heimdall.protocol.core.ProtocolClient;

import java.io.IOException;
import java.util.Date;

public class SyslogClient extends ProtocolClient<SyslogMessage> {

    private final SyslogProperties properties = new SyslogProperties();
    private final SyslogMessageSender sender = createSyslogSender();

    @Override
    protected int getDefaultPort() {
        return 514;
    }

    /**
     * Subclasses will implement this method to send an event.
     *
     * @param event the event
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doSend(SyslogMessage event) throws IOException {
        com.cloudbees.syslog.SyslogMessage syslogMessage =
                createMessage(event.getBodyAsString(), event.getSyslogSeverity(), event.getFacility());
        sender.sendMessage(syslogMessage);
    }

    private com.cloudbees.syslog.SyslogMessage createMessage(String message, Severity severity,
                                                            Facility facility) {
        com.cloudbees.syslog.SyslogMessage syslogMessage = new com.cloudbees.syslog.SyslogMessage();
        syslogMessage.setAppName("Heimdall");
        syslogMessage.setFacility(facility);
        syslogMessage.setSeverity(severity);
        syslogMessage.setHostname(getAddress().getHostName());
        syslogMessage.setProcId("test");
        //syslogMessage.setMsgId(Integer.toString(IDENTIFIER.getAndIncrement()));
        syslogMessage.withMsg(message);
        syslogMessage.setTimestamp(new Date());
        return syslogMessage;
    }

    private SyslogMessageSender createSyslogSender() {
        AbstractSyslogMessageSender messageSender;
        if (getTransport().equals(Transport.UDP)) {
            UdpSyslogMessageSender udpMessageSender = new UdpSyslogMessageSender();
            udpMessageSender.setSyslogServerPort(properties.getUdpPort());
            messageSender = udpMessageSender;
        } else {
            TcpSyslogMessageSender tcpMessageSender = new TcpSyslogMessageSender();
            tcpMessageSender.setSyslogServerPort(properties.getTcpPort());
            messageSender = tcpMessageSender;
        }
        messageSender.setSyslogServerHostname("localhost");
        messageSender.setDefaultMessageHostname("localhost");
        messageSender.setMessageFormat(MessageFormat.RFC_5424);
        return messageSender;
    }

    public SyslogProperties getSyslogConfiguration() {
        return properties;
    }

    public SyslogMessageSender getSender() {
        return sender;
    }
}
