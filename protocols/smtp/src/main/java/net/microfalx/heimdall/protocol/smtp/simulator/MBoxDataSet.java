package net.microfalx.heimdall.protocol.smtp.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolUtils;
import net.microfalx.heimdall.protocol.core.simulator.AbstractProtocolDataSet;
import net.microfalx.heimdall.protocol.core.simulator.AbstractProtocolDataSetFactory;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.IOUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;
import org.apache.james.mime4j.mboxiterator.CharBufferWrapper;
import org.apache.james.mime4j.mboxiterator.MboxIterator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MBoxDataSet extends AbstractProtocolDataSet<MimeMessage, Field<MimeMessage>, String> {

    protected static final Metrics METRICS = ProtocolUtils.getMetrics(Event.Type.SMTP).withGroup("Data Set");

    public MBoxDataSet(Resource resource) {
        super(resource);
    }

    @Override
    public Metadata<MimeMessage, Field<MimeMessage>, String> getMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected MBoxDataSet updateName(String name) {
        setName(name);
        return this;
    }

    @Override
    public @NotNull Iterator<MimeMessage> iterator() {
        CharsetEncoder ENCODER = StandardCharsets.UTF_8.newEncoder();
        try {
            MboxIterator iterator = MboxIterator.fromFile(getFile()).charset(ENCODER.charset()).build();
            return new IteratorImpl(iterator);
        } catch (IOException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    private void countMessage() {
        METRICS.count(getName());
    }

    public static abstract class Factory extends AbstractProtocolDataSetFactory<MimeMessage, Field<MimeMessage>, String> {

        public Factory(Resource resource) {
            super(resource);
        }
    }

    private class IteratorImpl implements Iterator<MimeMessage> {

        private final MboxIterator mboxIterator;
        private final Iterator<CharBufferWrapper> iterator;
        private int index = 1;

        public IteratorImpl(MboxIterator mboxIterator) {
            this.mboxIterator = mboxIterator;
            this.iterator = mboxIterator.iterator();
        }

        @Override
        public boolean hasNext() {
            boolean next = iterator.hasNext();
            if (!next) IOUtils.closeQuietly(mboxIterator);
            return next;
        }

        @Override
        public MimeMessage next() {
            countMessage();
            return new MimeMessage().setIndex(index++)
                    .setMailbox(MBoxDataSet.this.getName())
                    .setContent(iterator.next().toString());
        }
    }
}
