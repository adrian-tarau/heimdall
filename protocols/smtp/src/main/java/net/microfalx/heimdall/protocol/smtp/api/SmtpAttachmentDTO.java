package net.microfalx.heimdall.protocol.smtp.api;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class SmtpAttachmentDTO {
    private long id;
    private SmtpDTO smtpDTO;
    private Attachment attachment;
}
