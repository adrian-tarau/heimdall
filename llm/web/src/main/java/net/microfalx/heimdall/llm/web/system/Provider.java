package net.microfalx.heimdall.llm.web.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;

@Entity(name = "WebProvider")
@Table(name = "llm_provider")
@Name("Providers")
@Getter
@Setter
public class Provider extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Position(2)
    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @Description("The natural id of the provider")
    @Visible(value = false)
    private String naturalId;

    @Position(10)
    @Column(name = "uri", length = 1000)
    @Description("The URI of the provider")
    @Visible(value = false)
    private String uri;

    @Position(15)
    @Column(name = "api_key", nullable = false, length = 500)
    @Description("the API key to use when accessing the model")
    @Visible(value = false)
    @Component(Component.Type.PASSWORD)
    private String apiKey;

    @Position(20)
    @Column(name = "author", nullable = false, length = 100)
    @Description("The author of the provider")
    private String author;

    @Position(100)
    @Column(name = "license", length = 1000)
    @Description("The license of the provider")
    private String license;

    @Position(200)
    @Column(name = "version", length = 50)
    @Description("The version of the provider")
    private String version;
}
