package net.microfalx.heimdall.llm.web;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.heimdall.llm.core.Model;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "WebChatModel")
@Table(name = "llm_chat")
@Name("Chats")
@Getter
@Setter
public class Chat extends IdentityAware<Integer> {

    @NaturalId
    @Position(2)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @Description("The natural id of the model")
    @Visible(value = false)
    private String naturalId;

    @Column(name = "name", nullable = false)
    @Position(5)
    @NotBlank
    @Name
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Position(10)
    @Column(name = "user_id", nullable = false)
    @Description("The user that created the chat")
    private String user;

    @ManyToOne
    @Position(15)
    @JoinColumn(name = "model_id", nullable = false)
    @Description("The model used by this chat session")
    private Model model;

    @Column(name = "start_at", nullable = false)
    @Position(20)
    @Description("The start time of chat")
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    @Position(25)
    @Description("The finish time of chat")
    private LocalDateTime finishAt;

    @Lob
    @Column(name = "content", columnDefinition = "longtext", nullable = false)
    @Position(30)
    @Description("The content of the chat")
    private String content;

    @Column(name = "tags")
    //@Component(Component.Type.TAG)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;

    @Column(name = "token_count", nullable = false)
    @Position(35)
    @Description("The token count of the chat")
    private int tokenCount;

    @Column(name = "duration", nullable = false)
    @Position(40)
    @Description("The duration of the chat")
    private Duration duration;
}
