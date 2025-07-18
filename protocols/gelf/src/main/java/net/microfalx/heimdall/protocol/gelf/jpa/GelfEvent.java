package net.microfalx.heimdall.protocol.gelf.jpa;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_gelf_events")
@Name("GELF")
@ReadOnly
@Getter
@Setter
@ToString(callSuper = true)
public class GelfEvent extends Event {

    @JoinColumn(name = "address_id")
    @ManyToOne
    @NotNull
    @Position(1)
    @Label("Host")
    @Description("The name of the host, source or application that sent the log event")
    @Width("160px")
    private Address host;

    @JoinColumn(name = "short_message_id")
    @OneToOne
    @Name
    @Position(5)
    @Label("Message")
    @Description("The short version of the message associated with the log event (the equivalent of the 'short_message' field in GELF)")
    @Filterable
    @Width("30%")
    private Part shortMessage;

    @JoinColumn(name = "long_message_id")
    @OneToOne
    @Position(6)
    @Visible(modes = Visible.Mode.VIEW)
    @Description("The full version of the message associated with the log event ((the equivalent of the 'full_message' field in GELF)")
    @Filterable
    private Part longMessage;

    @JoinColumn(name = "fields_id")
    @OneToOne
    @Position(10)
    @Visible(modes = Visible.Mode.VIEW)
    @Filterable
    private Part fields;

    @Column(name = "version", length = 50, nullable = false)
    @NotBlank
    @Position(40)
    @Visible(modes = Visible.Mode.VIEW)
    private String version;

    @Column(name = "application_name", nullable = false)
    @Position(25)
    @Description("Identifies the application (a collection of processes working together to form a complex application) which sent the log event")
    @Width("100px")
    private String application;

    @Column(name = "process_name", nullable = false)
    @Position(26)
    @Description("Identifies the process/service which sent the log event")
    @Width("100px")
    private String process;

    @Column(name = "logger_name", nullable = false)
    @Position(27)
    @Description("Identifies the logger (class/package) which logged of the log event")
    @Width("15%")
    private String logger;

    @Column(name = "thread_name", nullable = false)
    @Position(28)
    @Description("Identifies the process thread which logged of the log event")
    @Width("100px")
    private String thread;

    @Column(name = "level", nullable = false)
    @Position(35)
    @Filterable(value = true)
    @Description("Identifies the importance of the log event (the level equal to the standard syslog levels)")
    @Width("100px")
    private Severity level;

    @Column(name = "facility", nullable = false)
    @Position(36)
    @Filterable(value = true)
    @Description("Determines which process of the machine created the log event")
    @Visible(modes = Visible.Mode.VIEW)
    private Facility facility;

}
