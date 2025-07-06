package net.microfalx.heimdall.llm.web.system.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;

@Entity(name = "WebPrompt")
@Table(name = "llm_prompt")
@Name("Prompts")
@Getter
@Setter
@Tabs
public class Prompt extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @Description("The natural id of the model")
    @Visible(false)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "model_id")
    @Position(6)
    @Width("200px")
    @Description("The model associated with this prompt")
    private Model model;

    @Column(name = "question", length = 5000)
    @Position(6)
    @Label(value = "Question", group = "Fragments")
    @Description("The question or prompt text that will be presented to the model as the first message")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    @Component(Component.Type.TEXT_AREA)
    private String question;

    @Column(name = "role", length = 1000)
    @Position(10)
    @Label(value = "Identity", group = "Fragments")
    @Component(Component.Type.TEXT_AREA)
    @Description("Returns the identity of the prompt.The identity is used to define the context or purpose of the prompt in the chat completion.")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String role;

    @Column(name = "maximum_input_events")
    @Label(value = "Maximum Input Events", group = "Settings")
    @Position(15)
    @Description("The maximum number of input events allowed for the prompt")
    @Width("100px")
    private Integer maximumInputEvents;

    @Column(name = "maximum_output_tokens", nullable = false)
    @Label(value = "Maximum Output Tokens", group = "Settings")
    @Position(20)
    @Description("The maximum number of output tokens allowed for this prompt")
    @Width("100px")
    private Integer maximumOutputTokens;

    @Column(name = "chain_of_thought")
    @Label(value = "Chain of Thought", group = "Settings")
    @Position(25)
    @Description("Indicates whether the prompt uses chain of thought reasoning")
    @Width("100px")
    private boolean chainOfThought;

    @Column(name = "use_only_context")
    @Label(value = "Use only Context", group = "Settings")
    @Position(30)
    @Description("Indicates whether the prompt should use only the context without additional instructions")
    @Width("100px")
    private boolean useOnlyContext;

    @Lob
    @Column(name = "instructions")
    @Position(32)
    @Description("Instructions for the prompt, which can include additional context or guidelines for the model")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String instructions;

    @Column(name = "examples", length = 10000)
    @Position(35)
    @Label(value = "Examples", group = "Fragments")
    @Description("A collection of examples associated with the prompt")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String examples;

    @Column(name = "context", length = 1000)
    @Position(40)
    @Label(value = "Context", group = "Fragments")
    @Description("The context in which the prompt is used, e.g. 'chat', 'completion'")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String context;

    @Column(name = "thinking", nullable = false)
    @Position(42)
    @Description("Indicates whether the prompt is used for thinking or reasoning. This is typically used in chain of thought prompts")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private boolean thinking;

    @Column(name = "system", nullable = false)
    @Visible(modes = {Visible.Mode.BROWSE})
    @ReadOnly
    @Position(45)
    @Description("Indicates whether this is a system prompt (cannot be modified by users")
    private boolean system = false;
}
