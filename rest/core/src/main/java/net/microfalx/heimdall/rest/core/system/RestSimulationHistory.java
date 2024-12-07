package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.Description;

import java.time.LocalDateTime;

@Entity
@Table(name = "rest_simulation_history")
@Getter
@Setter
@ToString
public class RestSimulationHistory extends IdentityAware<Integer> {

    @OneToOne
    @JoinColumn(name = "rest_simulation_id",nullable = false)
    private RestSimulation restSimulation;

    @Column(name = "resource",nullable = false,length = 1000)
    private String resource;

    @Column(name = "version")
    private Integer version;

    @Column(name = "modified_by",length = 100)
    @Description("The user who modified the simulation")
    private String modifiedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
