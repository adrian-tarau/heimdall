package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;

@Entity(name = "CoreProvider")
@Table(name = "llm_provider")
@Name("Providers")
@Getter
@Setter
public class Provider extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Column(name = "uri", length = 1000)
    private String uri;

    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "license",length = 1000)
    private String license;

    @Column(name = "version",length = 50)
    private String version;
}
