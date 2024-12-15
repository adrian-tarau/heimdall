package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryHistory;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "rest_library_history")
@Getter
@Setter
@ToString(callSuper = true)
public class RestLibraryHistory extends AbstractLibraryHistory {

    @OneToOne
    @JoinColumn(name = "rest_library_id", nullable = false)
    @Position(5)
    private RestLibrary restLibrary;
}
