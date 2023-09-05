package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class SmtpService extends ProtocolService<SmtpEvent> {

    @Autowired
    private SmtpProperties configuration;
    @Autowired
    private SmtpEventRepository repository;
    @Autowired
    private SmtpSimulator smtpSimulator;

    @Autowired
    private SmtpAttachmentRepository attachmentRepository;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SMTP;
    }

    protected void persist(SmtpEvent smtpEvent) {
        net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent jpaSmtpEvent = new net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent();
        jpaSmtpEvent.setCreatedAt(smtpEvent.getCreatedAt().toLocalDateTime());
        jpaSmtpEvent.setSentAt(smtpEvent.getSentAt().toLocalDateTime());
        jpaSmtpEvent.setReceivedAt(smtpEvent.getReceivedAt().toLocalDateTime());
        jpaSmtpEvent.setSubject(smtpEvent.getName());
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
}
