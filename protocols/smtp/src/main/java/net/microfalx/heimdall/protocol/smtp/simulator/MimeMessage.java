package net.microfalx.heimdall.protocol.smtp.simulator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MimeMessage {

    private int index;
    private String mailbox;
    private String content;


}
