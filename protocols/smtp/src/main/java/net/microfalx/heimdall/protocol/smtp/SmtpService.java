package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import net.microfalx.heimdall.protocol.smtp.simulator.SmtpSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_NAME_LENGTH;

@Service
public final class SmtpService extends ProtocolService<SmtpEvent, net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent> {

    @Autowired
    private SmtpProperties properties;

    @Autowired
    private SmtpGateway gateway;

    @Autowired
    private SmtpEventRepository repository;

    @Autowired
    private SmtpSimulator smtpSimulator;

    @Autowired
    private SmtpServer smtpServer;

    @Autowired
    private SmtpAttachmentRepository attachmentRepository;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SMTP;
    }

    @Override
    protected String getControllerPath() {
        return "/protocol/smtp";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        smtpServer.addConsumer(this::accept);
    }

    @Override
    protected boolean isSimulatorEnabled(boolean enabled) {
        if (super.isSimulatorEnabled(enabled)) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
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

    private void updateAddresses(SmtpEvent smtpEvent, net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent jpaSmtpEvent) {
        jpaSmtpEvent.setFrom(lookupAddress(smtpEvent.getSource()));
        jpaSmtpEvent.setTo(lookupAddress(smtpEvent.getTargets().iterator().next()));
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


}
