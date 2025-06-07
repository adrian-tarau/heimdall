package net.microfalx.heimdall.llm.core;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Message;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for chat sessions.
 */
public abstract class AbstractChat extends NamedAndTaggedIdentifyAware<String> implements Chat {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChat.class);

    private final Model model;
    private final LocalDateTime startAt = LocalDateTime.now();
    private LocalDateTime finishAt;
    private int tokenCount;
    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;

    private Prompt prompt = Prompt.empty();

    private ChatMemory chatMemory;
    private SimpleChat chat;
    private StreamChat streamChat;
    private LlmServiceImpl service;

    private volatile Principal principal;

    public AbstractChat(Prompt prompt, Model model) {
        requireNonNull(prompt);
        requireNonNull(model);
        setId(UUID.randomUUID().toString());
        setName("Unnamed");
        this.prompt = prompt;
        this.model = model;
    }

    @Override
    public final Model getModel() {
        return model;
    }

    @Override
    public final Prompt getPrompt() {
        return prompt;
    }

    @Override
    public final Principal getUser() {
        if (principal == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                principal = (Principal) authentication.getPrincipal();
            }
            principal = new PrincipalImpl("anonymous");
        }
        return principal;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public LocalDateTime getStartAt() {
        return startAt;
    }

    @Override
    public LocalDateTime getFinishAt() {
        return finishAt;
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    @Override
    public Duration getDuration() {
        return Duration.between(startAt, finishAt != null ? finishAt : LocalDateTime.now());
    }

    @Override
    public Collection<Message> getMessages() {
        return List.of();
    }

    @Override
    public String ask(String message) {
        validate();
        if (chatModel != null) {
            String answer = chatModel.chat(message);
            tokenCount = StringUtils.split(answer, " ").length;
            return answer;
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<String> stream = chat(message);
            while (stream.hasNext()) {
                String token = stream.next();
                builder.append(token);
                tokenCount++;
            }
            return builder.toString();
        }
    }

    @Override
    public Iterator<String> chat(String message) {
        validate();
        if (streamingChatModel != null) {
            ResponseHandler responseHandler = new ResponseHandler();
            service.getChatPool().execute(() -> streamingChatModel.chat(message, responseHandler));
            return responseHandler;
        } else {
            String answer = ask(message);
            String[] parts = StringUtils.split(answer, " ");
            return Arrays.asList(parts).iterator();
        }
    }

    public final AbstractChat setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public final AbstractChat setStreamingChatModel(StreamingChatModel streamingChatModel) {
        requireNonNull(streamingChatModel);
        this.streamingChatModel = streamingChatModel;
        return this;
    }

    protected void doClose() throws IOException {
        // default implementation does nothing
    }

    private void validate() {
        if (chatModel == null && streamingChatModel == null) {
            throw new IllegalStateException("No chat model has been set");
        }
    }

    @Override
    public final void close() {
        finishAt = LocalDateTime.now();
        try {
            doClose();
        } catch (IOException e) {
            LOGGER.atWarn().setCause(e).log("Failed to close chat session {}", getNameAndId());
        }
        if (service != null) service.closeChat(this);
    }

    void initialize(LlmServiceImpl service) {
        requireNonNull(service);
        validate();
        this.service = service;
        this.prompt = prompt;
        HuggingFaceTokenCountEstimator tokenCountEstimator = new HuggingFaceTokenCountEstimator();
        this.chatMemory = TokenWindowChatMemory.builder()
                .maxTokens(model.getMaximumContextLength(), tokenCountEstimator)
                .chatMemoryStore(service.getChatStore())
                .build();
        if (chatModel != null) {
            chat = updateAiService(AiServices.builder(SimpleChat.class)).build();
        } else {
            streamChat = updateAiService(AiServices.builder(StreamChat.class)).build();
        }
    }

    private <T> AiServices<T> updateAiService(AiServices<T> aiService) {
        aiService.chatMemory(chatMemory)
                .systemMessageProvider(new SystemMessageProvider())
                .contentRetriever(service.getContentRetriever())
                .toolProvider(new ToolProviderImpl());
        if (streamingChatModel != null) {
            aiService.streamingChatModel(streamingChatModel);
        } else if (chatModel != null) {
            aiService.chatModel(chatModel);
        } else {
            throw new IllegalStateException("No chat model has been set");
        }
        return aiService;
    }

    public interface SimpleChat {

        String chat(String message);
    }

    public interface StreamChat {

        TokenStream chat(String message);
    }

    private class SystemMessageProvider implements Function<Object, String> {

        @Override
        public String apply(Object o) {
            return "";
        }
    }

    private class ToolProviderImpl implements ToolProvider {

        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            return null;
        }
    }

    private static class ResponseHandler implements StreamingChatResponseHandler, Iterator<String> {

        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private volatile Throwable throwable;

        @Override
        public void onPartialResponse(String partialResponse) {
            if (!queue.offer(partialResponse)) {
                LOGGER.atError().log("Queue is full : {}", partialResponse);
            }
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            completed.set(true);
        }

        @Override
        public void onError(Throwable error) {
            this.throwable = error;
            this.completed.set(true);
        }

        @Override
        public boolean hasNext() {
            raiseIfError();
            if (!queue.isEmpty()) return true;
            while (queue.isEmpty() && !completed.get()) {
                ThreadUtils.sleepMillis(5);
            }
            raiseIfError();
            return !(queue.isEmpty() || completed.get());
        }

        @Override
        public String next() {
            return queue.poll();
        }

        private void raiseIfError() {
            if (throwable != null) ExceptionUtils.throwException(throwable);
        }
    }

    private static class PrincipalImpl implements java.security.Principal {

        private final String name;

        public PrincipalImpl(String name) {
            requireNonNull(name);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
