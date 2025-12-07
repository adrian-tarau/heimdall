package net.microfalx.heimdall.protocol.smtp.api;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.protocol.core.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class SmtpDTO {
    private long id;
    private String subject;
    private Address from;
    private Address to;
    private Part message;
    private Collection<Attachment> attachments;
    private int attachmentCount;
    private LocalDateTime receivedAt;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;
}
