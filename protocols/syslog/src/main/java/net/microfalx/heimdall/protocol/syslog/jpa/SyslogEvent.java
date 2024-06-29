package net.microfalx.heimdall.protocol.syslog.jpa;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;

@Entity
@Table(name = "protocol_syslog_events")
@Name("Syslog")
@ReadOnly
@Getter
@Setter
@ToString(callSuper = true)
public class SyslogEvent extends Event {

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    @Position(1)
    @Description("the name of the host, source or application that sent the Syslog log event")
    private Address address;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @Name
    @Position(1)
    @Description("The content of the Syslog log event")
    @Filterable
    private Part message;

    @Column(name = "severity", nullable = false)
    @Position(10)
    @Description("Identify the importance of the Syslog log event")
    @Filterable(true)
    private Severity severity;

    @Column(name = "facility", nullable = false)
    @Position(10)
    @Description("Determines which process of the machine created the Syslog log event")
    @Filterable(true)
    private Facility facility;
}
