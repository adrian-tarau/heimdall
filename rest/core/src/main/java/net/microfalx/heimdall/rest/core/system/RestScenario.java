package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.UpdateStrategy;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "rest_scenario")
@Name("Scenarios")
@ToString
@Getter
@Setter
@UpdateStrategy(fieldNames = {"name", "description", "tags"})
public class RestScenario extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false,length = 100)
    @NaturalId
    @Description("The natural key of the scenario")
    @Position(2)
    @Visible(false)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The natural key of the scenario")
    @Position(3)
    private RestSimulation simulation;

    @Column(name = "tolerating_threshold", nullable = false)
    @Description("The threshold that the users will tolerate the service")
    @Position(10)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @UpdateStrategy()
    @Label(group = "Thresholds", value = "Tolerating")
    private int toleratingThreshold;

    @Column(name = "frustrating_threshold", nullable = false)
    @Description("The threshold that the users will be frustrated and stop using the service")
    @Position(11)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @UpdateStrategy()
    @Label(group = "Thresholds", value = "Frustrating")
    private int frustratingThreshold;

    @Column(name = "start_time")
    @Description("The start time for the scenario")
    @Position(20)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Integer startTime;

    @Column(name = "gracefulStop")
    @Description("The grace stop of the scenario")
    @Position(21)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Integer gracefulStop;

    @Column(name = "function", length = 100)
    @Description("The function")
    @Position(22)
    private String function;
}
