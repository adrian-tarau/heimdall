package net.microfalx.heimdall.llm.core;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for chat sessions.
 */
public abstract class AbstractChat extends NamedAndTaggedIdentifyAware<String> implements Chat {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChat.class);

    private final Model model;
    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;

    AiServiceImpl service;

    public AbstractChat(Model model) {
        requireNonNull(model);
        setId(UUID.randomUUID().toString());
        setName("Unnamed");
        this.model = model;
    }


    public final Model getModel() {
        return model;
    }

    @Override
    public String ask(String message) {
        validate();
        if (chatModel != null) {
            return chatModel.chat(message);
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<String> stream = chat(message);
            while (stream.hasNext()) {
                String token = stream.next();
                builder.append(token);
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

    public final ChatModel getChatModel() {
        return chatModel;
    }

    public final AbstractChat setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public final StreamingChatModel getStreamingChatModel() {
        return streamingChatModel;
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
        try {
            doClose();
        } catch (IOException e) {
            LOGGER.atWarn().setCause(e).log("Failed to close chat session {}", getNameAndId());
        }
        if (service != null) service.closeChat(this);
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
        }

        @Override
        public boolean hasNext() {
            if (throwable != null) ExceptionUtils.throwException(throwable);
            if (!queue.isEmpty()) return true;
            while (queue.isEmpty() && !completed.get()) {
                ThreadUtils.sleepMillis(5);
            }
            return !(queue.isEmpty() || completed.get());
        }

        @Override
        public String next() {
            return queue.poll();
        }
    }
}
