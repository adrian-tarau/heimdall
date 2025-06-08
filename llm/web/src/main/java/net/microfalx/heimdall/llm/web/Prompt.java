package net.microfalx.heimdall.llm.web;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.*;

@Entity(name = "WebPrompt")
@Table(name = "llm_prompt")
@Name("Prompts")
@Getter
@Setter
public class Prompt extends IdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @Description("The natural id of the model")
    @Visible(false)
    private String naturalId;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Position(5)
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Column(name = "role", length = 1000)
    @Position(10)
    @Description("The role of the prompt, e.g. 'system', 'user', 'assistant'")
    @Visible(false)
    private String role;

    @Column(name = "maximum_input_events")
    @Label(value = "Maximum Input Tokens", group = "Settings")
    @Position(15)
    @Description("The maximum number of input events allowed for this prompt")
    private Integer maximumInputEvents;

    @Column(name = "maximum_output_tokens", nullable = false)
    @Label(value = "Maximum Output Tokens", group = "Settings")
    @Position(20)
    @Description("The maximum number of output tokens allowed for this prompt")
    private Integer maximumOutputTokens;

    @Column(name = "chain_of_thought")
    @Label(value = "Chain of Thought", group = "Settings")
    @Position(25)
    @Description("Indicates whether the prompt uses chain of thought reasoning")
    private boolean chainOfThought;

    @Column(name = "use_only_context")
    @Label(value = "Use only Context", group = "Settings")
    @Position(30)
    @Description("Indicates whether the prompt should use only the context without additional instructions")
    private boolean useOnlyContext;

    @Column(name = "examples", length = 10000)
    @Position(35)
    @Description("A collection of examples associated with the prompt")
    @Visible(false)
    private String examples;

    @Column(name = "context", length = 1000)
    @Position(40)
    @Description("The context in which the prompt is used, e.g. 'chat', 'completion'")
    @Visible(false)
    private String context;

    @Column(name = "question", length = 5000)
    @Position(50)
    @Description("The question or prompt text that will be presented to the model")
    @Visible(false)
    private String question;

    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
