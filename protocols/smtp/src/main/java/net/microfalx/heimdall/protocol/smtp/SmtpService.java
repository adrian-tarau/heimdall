package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.internet.MimeMessage;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_NAME_LENGTH;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Service
public final class SmtpService extends ProtocolService<SmtpEvent, net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent> {

    @Autowired
    private SmtpProperties configuration;

    @Autowired
    private SmtpGatewayProperties gatewayConfiguration;

    @Autowired
    private SmtpEventRepository repository;

    @Autowired
    private SmtpSimulator smtpSimulator;

    @Autowired
    private SmtpAttachmentRepository attachmentRepository;

    private JavaMailSender mailSender;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SMTP;
    }

    @Override
    protected String getControllerPath() {
        return "/protocol/smtp";
    }

    /**
     * Forwards an email to real accounts.
     *
     * @param resource the resource containing the email MIME message
     */
    public void forward(Resource resource) {
        requireNonNull(resource);
        try {
            JavaMailSenderImpl mailSender = createMailSender();
            MimeMessage mimeMessage = mailSender.createMimeMessage(resource.getInputStream());
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            rethrowException(e);
        }
    }

    @Override
    protected void prepare(SmtpEvent event) {
        net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent jpaSmtpEvent = new net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent();
        updateAddresses(event, jpaSmtpEvent);
    }

    protected void persist(SmtpEvent smtpEvent) {
        net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent jpaSmtpEvent = new net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent();
        jpaSmtpEvent.setCreatedAt(smtpEvent.getCreatedAt().toLocalDateTime());
        jpaSmtpEvent.setSentAt(smtpEvent.getSentAt().toLocalDateTime());
        jpaSmtpEvent.setReceivedAt(smtpEvent.getReceivedAt().toLocalDateTime());
        Body messageBody = Body.create(smtpEvent.getResource().orElseThrow(), smtpEvent);
        jpaSmtpEvent.setMessage(persistPart(messageBody));
        jpaSmtpEvent.setSubject(org.apache.commons.lang3.StringUtils.abbreviate(smtpEvent.getName(), MAX_NAME_LENGTH));
        updateAddresses(smtpEvent, jpaSmtpEvent);
        List<SmtpAttachment> attachments = smtpEvent.getParts().stream().map(part -> {
            SmtpAttachment attachment = new SmtpAttachment();
            attachment.setSmtpEvent(jpaSmtpEvent);
            attachment.setPart(persistPart(part));
            return attachment;
        }).toList();
        jpaSmtpEvent.setAttachmentCount((int) smtpEvent.getParts().stream().filter(part -> part.getType() == Part.Type.ATTACHMENT).count());
        repository.save(jpaSmtpEvent);
        attachments.forEach(jpaSmtpEvent::addAttachment);
        attachmentRepository.saveAll(attachments);
        updateReference(smtpEvent, jpaSmtpEvent.getId());
    }

    private void updateAddresses(SmtpEvent smptEvent, net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent smtpEvent) {
        smtpEvent.setFrom(lookupAddress(smptEvent.getSource()));
        smtpEvent.setTo(lookupAddress(smptEvent.getTargets().iterator().next()));
    }

    /**
     * Returns the simulator.
     *
     * @return the simulator, null if not supported
     */
    @Override
    protected SmtpSimulator getSimulator() {
        return smtpSimulator;
    }

    public JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(gatewayConfiguration.getHost());
        sender.setPort(gatewayConfiguration.getPort());
        Properties props = sender.getJavaMailProperties();
        if (isNotEmpty(gatewayConfiguration.getUserName())) {
            props.put("mail.smtp.auth", "true");
            sender.setUsername(gatewayConfiguration.getUserName());
            sender.setPassword(gatewayConfiguration.getPassword());
        } else {
            props.put("mail.smtp.auth", "false");
        }
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", gatewayConfiguration.isTls());
        mailSender = sender;
        return sender;
    }
}
