package net.microfalx.heimdall.rest.core.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;
import net.microfalx.lang.annotation.Description;

import java.time.LocalDateTime;

@MappedSuperclass
@ToString(callSuper = true)
@Getter
@Setter
public abstract class AbstractLibraryHistory extends IdentityAware<Integer> {

    @Column(name = "resource",nullable = false,length = 1000)
    private String resource;

    @Column(name = "version")
    private Integer version;

    @Column(name = "modified_by",length = 100)
    @Description("The user who modified the simulation")
    private String modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
