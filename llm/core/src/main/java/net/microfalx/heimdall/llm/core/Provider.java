package net.microfalx.heimdall.llm.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "provider")
@Name("Providers")
@Getter
@Setter
public class Provider extends NamedIdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Position(15)
    @Column(name = "uri", length = 1000)
    private String uri;

    @Position(20)
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Position(25)
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "tags")
    @Position(400)
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    @Position(500)
    @Column(name = "license",length = 1000)
    private String license;

    @Position(600)
    @Column(name = "version",length = 50)
    private String version;
}
