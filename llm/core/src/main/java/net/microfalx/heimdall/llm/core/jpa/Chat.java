package net.microfalx.heimdall.llm.core.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.jdbc.entity.natural.NamedAndTaggedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.lang.annotation.Name;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "CoreChatModel")
@Table(name = "llm_chat")
@Name("Chats")
@Getter
@Setter
public class Chat extends NamedAndTaggedIdentityAware<String> {

    @Column(name = "user_id", nullable = false)
    private String user;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    private LocalDateTime finishAt;

    @Column(name = "resource", nullable = false)
    private String resource;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "duration", nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration duration;
}
