package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Width;

@Entity(name = "CoreProvider")
@Table(name = "llm_provider")
@Name("Providers")
@Getter
@Setter
public class Provider extends NamedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Column(name = "uri", length = 1000)
    private String uri;

    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    @Column(name = "license",length = 1000)
    private String license;

    @Column(name = "version",length = 50)
    private String version;
}
