package net.microfalx.heimdall.llm.core;

import dev.langchain4j.service.TokenStream;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class TokenStreamHandler implements Iterator<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenStreamHandler.class);

    private final AbstractChat chat;
    private final LlmServiceImpl llmService;
    private final TokenStream tokenStream;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private volatile Throwable throwable;

    TokenStreamHandler(LlmServiceImpl llmService, AbstractChat chat, TokenStream tokenStream) {
        requireNonNull(llmService);
        requireNonNull(chat);
        requireNonNull(tokenStream);
        this.llmService = llmService;
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

    private void raiseIfError() {
        if (throwable != null) ExceptionUtils.throwException(throwable);
    }

    private void initTokenStream() {
        tokenStream.onError(t -> {
            this.throwable = t;
            this.completed.set(true);
        });
        tokenStream.onPartialResponse(r -> {
            if (!queue.offer(r)) {
                LOGGER.atError().log("Queue is full for chat {}: {}", chat.getName(), r);
            }
        });
        tokenStream.onCompleteResponse(r -> {
            completed.set(true);
        });
    }
}
