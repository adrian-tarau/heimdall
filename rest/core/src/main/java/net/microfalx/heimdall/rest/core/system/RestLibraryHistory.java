package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.Description;

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

    @Column(name = "modified_by",length = 100)
    @Description("The user who modified the library")
    private String modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
