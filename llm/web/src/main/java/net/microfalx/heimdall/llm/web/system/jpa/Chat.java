package net.microfalx.heimdall.llm.web.system.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.natural.NamedAndTaggedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.heimdall.llm.core.jpa.Model;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "WebChatModel")
@Table(name = "llm_chat")
@Name("Chats")
@Getter
@Setter
@ReadOnly
public class Chat extends NamedAndTaggedIdentityAware<Integer> {

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
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    @Position(25)
    @Description("The finish time of chat")
    private LocalDateTime finishAt;

    @Column(name = "resource", nullable = false)
    @Position(30)
    @Description("The content of the chat")
    @Visible(false)
    private String content;

    @Column(name = "token_count", nullable = false)
    @Position(35)
    @Description("The token count of the chat")
    private int tokenCount;

    @Column(name = "duration", nullable = false)
    @Position(40)
    @Description("The duration of the chat")
    @Convert(converter = DurationConverter.class)
    private Duration duration;
}
