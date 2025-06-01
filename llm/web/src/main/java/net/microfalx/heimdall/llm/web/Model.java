package net.microfalx.heimdall.llm.web;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.heimdall.llm.api.ResponseFormat;
import net.microfalx.lang.annotation.*;

@Entity(name = "WebModel")
@Table(name = "llm_model")
@Name("Models")
@Getter
@Setter
@Tabs
public class Model extends NamedIdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @Description("The natural id of the model")
    @Visible(value = false)
    private String naturalId;

    @Position(10)
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    @Description("The provider")
    @ReadOnly
    private Provider provider;

    @Position(15)
    @Column(name = "uri", length = 1000)
    @Description("The URI of the model")
    @Visible(value = false)

    private String uri;

    @Position(20)
    @Column(name = "api_key", nullable = false, length = 500)
    @Description("The API key to use when accessing the model")
    @Visible(value = false)
    @Component(Component.Type.PASSWORD)
    private String apiKey;

    @Position(21)
    @Column(name = "enabled", nullable = false)
    @Description("The default model use for inference")
    private boolean enabled;

    @Position(22)
    @Column(name = "default", nullable = false)
    @Description("The default model use for inference")
    @Label(value = "Default")
    private boolean _default;

    @Position(22)
    @Column(name = "embedding", nullable = false)
    @Description("The embedded model use for inference")
    @Label(value = "Embedding")
    private boolean embedding;

    @Position(25)
    @Column(name = "model_name", length = 100)
    @Description("A reference to the model name")
    @Visible(value = false)
    @ReadOnly
    private String modelName;

    @Position(30)
    @Column(name = "temperature")
    @Label(value = "Temperature", group = "Sampling")
    @Description("The sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output\n" +
            "more random, while lower values like 0.2 will make it more focused and deterministic")
    private Double temperature;

    @Position(40)
    @Column(name = "top_p")
    @Label(value = "Top-P", group = "Sampling")
    @Description("An alternative to sampling with temperature, called nucleus sampling, where the model considers\n" +
            "the results of the tokens with top_p probability mass")
    private Double topP;

    @Position(45)
    @Column(name = "top_k")
    @Label(value = "Top-K", group = "Sampling")
    @Description("""
            The number of most likely tokens to keep for top-k sampling.
            The parameter limits the model’s output to the top-k most probable tokens at each step. This can help reduce
            incoherent or nonsensical output by restricting the model’s vocabulary""")
    private Integer topK;

    @Position(50)
    @Column(name = "frequency_penalty")
    @Label(value = "Frequency", group = "Penality")
    @Description("""
            The frequency penalty to use, between -2 and 2. Positive values penalize new tokens based on
            their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line
            verbatim.""")
    private Double frequencyPenalty;

    @Position(55)
    @Column(name = "presence_penalty")
    @Label(value = "Presence", group = "Penality")
    @Description("The presence penalty to use, between -2 and 2. Positive values penalize new tokens based on\n" +
            "whether they appear in the text so far, increasing the model's likelihood to talk about new topics.")
    private Double presencePenalty;

    @Position(60)
    @Column(name = "maximum_output_tokens")
    @Label(value = "Maximum Output Tokens", group = "Other")
    @Description("The maximum number of tokens that can be generated in the chat completion")
    @Visible(value = false)
    private Integer maximumOutputTokens;

    @Position(65)
    @Column(name = "stop_sequences", nullable = false, length = 1000)
    @Label(value = "Stop Sequences", group = "Other")
    @Description("The tokens to be used as a stop sequence. The API will stop generating further tokens after\n" +
            "it encounters the stop sequence")
    @Visible(value = false)
    private String stopSequences;

    @Position(70)
    @Column(name = "response_format", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Description("The format of the response from the model")
    @Visible(value = false)
    private ResponseFormat responseFormat;

    @Position(400)
    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
