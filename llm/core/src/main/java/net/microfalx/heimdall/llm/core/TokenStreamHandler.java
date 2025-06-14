package net.microfalx.heimdall.llm.core;

import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.TokenStream;
import net.microfalx.heimdall.llm.api.FinishReason;
import net.microfalx.lang.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Handles a token stream from a chat session, allowing iteration over the tokens.
 */
class TokenStreamHandler extends AbstractTokenStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenStreamHandler.class);

    private final AbstractChat chat;
    private final LlmServiceImpl service;
    private final TokenStream tokenStream;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    TokenStreamHandler(LlmServiceImpl service, AbstractChat chat, TokenStream tokenStream) {
        requireNonNull(service);
        requireNonNull(chat);
        requireNonNull(tokenStream);
        this.service = service;
        this.chat = chat;
        this.tokenStream = tokenStream;
        initTokenStream();
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
        if (queue.isEmpty()) throw new IllegalStateException("Queue is empty");
        return queue.poll();
    }

    @Override
    public boolean isComplete() {
        return queue.isEmpty() && super.isComplete();
    }

    private void initTokenStream() {
        tokenStream.onError(t -> {
            this.throwable = t;
            this.completed.set(true);
        });
        tokenStream.onPartialResponse(r -> {
            builder.append(r);
            if (!queue.offer(r)) {
                LOGGER.atError().log("Queue is full for chat {}: {}", chat.getName(), r);
            }
        });
        tokenStream.onCompleteResponse(r -> {
            message = MessageImpl.create(r.aiMessage());
            finishReason = toFinishReason(r.finishReason());
            TokenUsage tokenUsage = r.tokenUsage();
            if (tokenUsage != null) {
                this.inputTokenCount = tokenUsage.inputTokenCount();
                this.outputTokenCount = tokenUsage.outputTokenCount();
            }
            completed.set(true);
        });
    }

    private FinishReason toFinishReason(dev.langchain4j.model.output.FinishReason finishReason) {
        if (finishReason == null) return null;
        return switch (finishReason) {
            case STOP -> FinishReason.STOP;
            case LENGTH -> FinishReason.LENGTH;
            case CONTENT_FILTER -> FinishReason.CONTENT_FILTER;
            case TOOL_EXECUTION -> FinishReason.TOOL_EXECUTION;
            case OTHER -> FinishReason.OTHER;
        };
    }
}
