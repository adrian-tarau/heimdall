package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.heimdall.llm.api.Content;
import net.microfalx.heimdall.llm.api.Message;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.resource.ResourceUtils.loadAsString;

@Getter
@ToString
public class MessageImpl implements Message {

    private final Type type;
    private final List<Content> contents = new ArrayList<>();

    public static MessageImpl create(ChatMessage message) {
        return new MessageImpl(getType(message), getContent(message));
    }

    public MessageImpl(Type type) {
        this.type = type;
    }

    public MessageImpl(Type type, Collection<Content> contents) {
        this.type = type;
        this.contents.addAll(contents);
    }

    @Override
    public Type type() {
        return type;
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
            return userMessage.contents().stream().map(c -> (Content) ContentImpl.from(c)).toList();
        } else if (message instanceof AiMessage aiMessage) {
            return List.of(ContentImpl.from(aiMessage.text()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Message.Type getType(ChatMessage message) {
        return switch (message) {
            case SystemMessage m -> Type.SYSTEM;
            case UserMessage m -> Type.USER;
            case AiMessage m -> Type.MODEL;
            case null, default -> Type.CUSTOM;
        };
    }

}
