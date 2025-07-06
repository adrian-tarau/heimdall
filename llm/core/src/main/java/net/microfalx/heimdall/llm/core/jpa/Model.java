package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.llm.api.ResponseFormat;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;

@Entity(name = "CoreModel")
@Table(name = "llm_model")
@Name("Models")
@Getter
@Setter
public class Model extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

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

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "default", nullable = false)
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

    @Column(name = "thinking", nullable = false)
    boolean thinking;

    @Column(name = "maximum_context_length", nullable = false)
    private int maximumContextLength;

    @Column(name = "maximum_output_tokens")
    private Integer maximumOutputTokens;

    @Column(name = "stop_sequences", nullable = false, length = 1000)
    private String stopSequences;

    @Column(name = "response_format", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ResponseFormat responseFormat;

    public boolean isDefault() {
        return _default;
    }

    public Model setDefault(boolean _default) {
        this._default = _default;
        return this;
    }
}
