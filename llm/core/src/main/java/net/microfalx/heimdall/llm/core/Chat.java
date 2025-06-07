package net.microfalx.heimdall.llm.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.heimdall.llm.core.jpa.Model;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Width;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "CoreChatModel")
@Table(name = "llm_chat")
@Name("Chats")
@Getter
@Setter
public class Chat extends IdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Column(name = "user_id", nullable = false)
    private String user;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    private LocalDateTime finishAt;

    @Lob
    @Column(name = "content", columnDefinition = "longtext", nullable = false)
    private String content;

    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "duration", nullable = false)
    private Duration duration;
}
