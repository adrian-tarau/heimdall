package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "rest_simulation")
@Name("Simulation")
@ToString
@Getter
@Setter
public class RestSimulation extends AbstractLibrary {

    @Column(name = "timeout", nullable = false)
    @Description("The timeout associated with the simulation")
    @Position(7)
    @Formattable(unit = Formattable.Unit.SECOND)
    private int timeout;

}