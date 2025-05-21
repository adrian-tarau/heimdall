package net.microfalx.heimdall.llm.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.heimdall.llm.api.ResponseFormat;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "model")
@Name("Models")
@Getter
@Setter
public class Model extends NamedIdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Position(10)
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Position(9)
    @Column(name = "uri", length = 1000)
    private String uri;

    @Position(10)
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;

    @Position(15)
    @Column(name = "model_name", length = 100)
    private String modelName;

    @Position(20)
    @Column(name = "temperature")
    private Double temperature;

    @Position(25)
    @Column(name = "top_p")
    private Double topP;

    @Position(30)
    @Column(name = "top_k")
    private Integer topK;

    @Position(35)
    @Column(name = "frequency_penalty")
    private Double frequencyPenalty;

    @Position(40)
    @Column(name = "presence_penalty")
    private Double presencePenalty;

    @Position(45)
    @Column(name = "maximum_output_tokens")
    private Integer maximumOutputTokens;

    @Position(50)
    @Column(name = "stop_sequences", nullable = false, length = 1000)
    private String stopSequences;

    @Position(55)
    @Column(name = "response_format", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ResponseFormat responseFormat;

    @Column(name = "tags")
    @Position(400)
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
