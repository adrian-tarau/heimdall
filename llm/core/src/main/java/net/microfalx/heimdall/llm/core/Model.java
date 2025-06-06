package net.microfalx.heimdall.llm.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.heimdall.llm.api.ResponseFormat;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Width;

@Entity(name = "CoreModel")
@Table(name = "llm_model")
@Name("Models")
@Getter
@Setter
public class Model extends NamedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "uri", length = 1000)
    private String uri;

    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Column(name = "enabled", columnDefinition = "boolean default false", nullable = false)
    private boolean enabled;

    @Column(name = "default", columnDefinition = "boolean default false", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean _default;

    @Column(name = "embedding", nullable = false)
    private boolean embedding;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "top_p")
    private Double topP;

    @Column(name = "top_k")
    private Integer topK;

    @Column(name = "frequency_penalty")
    private Double frequencyPenalty;

    @Column(name = "presence_penalty")
    private Double presencePenalty;

    @Column(name = "maximum_output_tokens")
    private Integer maximumOutputTokens;

    @Column(name = "stop_sequences", nullable = false, length = 1000)
    private String stopSequences;

    @Column(name = "response_format", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ResponseFormat responseFormat;

    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    public boolean isDefault() {
        return _default;
    }

    public Model setDefault(boolean _default) {
        this._default = _default;
        return this;
    }
}
