package net.microfalx.heimdall.llm.core;

import java.util.Iterator;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class TokenStreamImpl extends AbstractTokenStream {

    private final Iterator<String> streams;

    public TokenStreamImpl(Iterator<String> streams) {
        requireNonNull(streams);
        this.streams = streams;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = streams.hasNext();
        completed.set(!hasNext);
        return hasNext;
    }

    @Override
    public String next() {
        String token = streams.next();
        builder.append(token);
        return token;
    }
}
