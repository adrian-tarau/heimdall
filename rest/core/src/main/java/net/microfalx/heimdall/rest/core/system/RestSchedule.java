package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.rest.core.common.AbstractSchedule;
import net.microfalx.lang.annotation.Name;

@Entity
@Table(name = "rest_schedule")
@Name("Schedules")
@ToString
@Getter
@Setter
public class RestSchedule extends AbstractSchedule {

}
