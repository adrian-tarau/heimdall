package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.FinishReason;
import net.microfalx.heimdall.llm.api.Message;
import net.microfalx.heimdall.llm.api.TokenStream;
import net.microfalx.lang.ExceptionUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for token streams.
 */
public abstract class AbstractTokenStream implements TokenStream {

    final AtomicBoolean completed = new AtomicBoolean(false);
    final StringBuilder builder = new StringBuilder();
    FinishReason finishReason = FinishReason.STOP;
    volatile Throwable throwable;
    volatile Message message;
    volatile Integer inputTokenCount;
    volatile Integer outputTokenCount;

    @Override
    public final Message getMessage() {
        if (message != null) {
            return message;
        } else {
            return MessageImpl.create(Message.Type.MODEL, builder.toString());
        }
    }

    @Override
    public final FinishReason getFinishReason() {
        return finishReason;
    }

    @Override
    public boolean isComplete() {
        return completed.get();
    }

    @Override
    public final Integer getInputTokenCount() {
        return inputTokenCount;
    }

    @Override
    public final Integer getOutputTokenCount() {
        return outputTokenCount;
    }

    protected final void raiseIfError() {
        if (throwable != null) ExceptionUtils.throwException(throwable);
    }
}
