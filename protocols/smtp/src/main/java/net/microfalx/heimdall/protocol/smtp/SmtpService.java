package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static net.microfalx.resource.ResourceUtils.requireNonNull;

@Service
public class SmtpService extends ProtocolService<Email> {

    @Autowired
    private SmtpConfiguration configuration;

    /**
     * Handle one SMTP message (email).
     * <p>
     * The method stores the email in the database, along with attachments. The email body and/or attachments are
     * stored in the shared data store and referenced as a {@link net.microfalx.resource.Resource}.
     *
     * @param email the STMP message
     */
    public void handle(Email email) {
        requireNonNull(email);
    }


}
