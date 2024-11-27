package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;

import java.time.LocalDateTime;

@Entity
@Table(name = "rest_library_history")
@Getter
@Setter
@ToString
public class RestLibraryHistory extends IdentityAware<Integer> {

    @OneToOne
    @JoinColumn(name = "rest_library_id", nullable = false)
    private RestLibrary restLibrary;

    @Column(name = "resource", nullable = false, length = 1000)
    private String resource;

    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
