package net.microfalx.heimdall.protocol.smtp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service which acts as an SMTP server.
 * <p>
 * The service listens for incoming emails, process them and stores the content in external storage and database.
 * <p>
 * The service uses <a href="https://github.com/davidmoten/subethasmtp">SubEtha SMTP</a> as an SMTP server.
 */
@Service
public class SmtpServerService {

    @Autowired
    private SmtpConfiguration configuration;

    @Autowired
    private SmtpService smtpService;

    @PostConstruct
    protected void initialize() {

    }
}
