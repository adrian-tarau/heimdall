package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.message.*;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.heimdall.llm.api.Content;
import net.microfalx.heimdall.llm.api.Message;
import net.microfalx.lang.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.resource.ResourceUtils.loadAsString;

@Getter
@ToString
public class MessageImpl implements Message {

    private final String id = UUID.randomUUID().toString();
    private final Type type;
    private final List<Content> contents = new ArrayList<>();
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public static Message create(ChatMessage message) {
        return new MessageImpl(getType(message), getContent(message));
    }

    public static Message create(Message.Type type, String text) {
        return new MessageImpl(type, List.of(ContentImpl.from(text)));
    }

    public MessageImpl(Type type) {
        this(type, emptyList());
    }

    public MessageImpl(Type type, Collection<Content> contents) {
        requireNonNull(type);
        requireNonNull(contents);
        this.type = type;
        this.contents.addAll(contents);
    }

    @Override
    public List<Content> getContent() {
        return unmodifiableList(contents);
    }

    @Override
    public String getText() {
        if (contents.isEmpty()) {
            return StringUtils.EMPTY_STRING;
        } else {
            StringBuilder sb = new StringBuilder();
            for (Content content : contents) {
                sb.append(loadAsString(content.getResource()));
            }
            return sb.toString();
        }
    }

    private static List<Content> getContent(ChatMessage message) {
        if (message instanceof SystemMessage systemMessage) {
            return List.of(ContentImpl.from(systemMessage.text()));
        } else if (message instanceof UserMessage userMessage) {
            return userMessage.contents().stream().map(ContentImpl::from).toList();
        } else if (message instanceof AiMessage aiMessage) {
            return List.of(ContentImpl.from(aiMessage.text()));
        } else if (message instanceof ToolExecutionResultMessage toolExecutionResultMessage) {
            return List.of(ContentImpl.from(toolExecutionResultMessage.text()));
        } else if (message == null) {
            return emptyList();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Message.Type getType(ChatMessage message) {
        return switch (message) {
            case SystemMessage m -> Type.SYSTEM;
            case ToolExecutionResultMessage m -> Type.TOOL;
            case UserMessage m -> Type.USER;
            case AiMessage m -> Type.MODEL;
            case null, default -> Type.CUSTOM;
        };
    }

}
