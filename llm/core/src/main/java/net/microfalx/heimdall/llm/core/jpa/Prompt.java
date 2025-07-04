package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.UpdateStrategy;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;

@Entity(name = "CorePrompt")
@Table(name = "llm_prompt")
@Name("Prompts")
@Getter
@Setter
public class Prompt extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "model_id")
    @UpdateStrategy
    private Model model;

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

    @Column(name = "system",nullable = false)
    private boolean system = false;
}
