package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.*;

@Entity(name = "CorePrompt")
@Table(name = "llm_prompt")
@Name("Prompts")
@Getter
@Setter
public class Prompt extends IdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Position(5)
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Column(name = "role", length = 1000)
    private String role;

    @Column(name = "maximum_input_events")
    private Integer maximumInputEvents;

    @Column(name = "maximum_output_tokens", nullable = false)
    private Integer maximumOutputTokens;

    @Column(name = "chain_of_thought")
    private boolean chainOfThought;

    @Column(name = "use_only_context")
    private boolean useOnlyContext;

    @Column(name = "examples", length = 10000)
    private String examples;

    @Column(name = "context", length = 1000)
    private String context;

    @Column(name = "question", length = 5000)
    private String question;

    @Column(name = "tags")
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
