package net.microfalx.heimdall.rest.core.overview;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.rest.core.common.AbstractSchedule;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

@Entity
@Table(name = "rest_schedule")
@Name("Schedules")
@ReadOnly
@ToString
@Getter
@Setter
public class Schedule extends AbstractSchedule {
}
