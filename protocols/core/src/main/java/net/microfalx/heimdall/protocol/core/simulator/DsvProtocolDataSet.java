package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.dsv.DsvDataSet;
import net.microfalx.bootstrap.dsv.DsvField;
import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Iterator;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;

public class DsvProtocolDataSet extends AbstractProtocolDataSet<DsvRecord, DsvField, String> {

    private Iterator<DsvRecord> iterator;

    public DsvProtocolDataSet(Resource resource) {
        super(resource);
    }

    @Override
    public Metadata<DsvRecord, DsvField, String> getMetadata() {
        try {
            return getDsvDataSet().getMetadata();
        } catch (Exception e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    @Override
    public Iterator<DsvRecord> iterator() {
        if (iterator == null || !iterator.hasNext()) {
            try {
                DsvDataSet dsvDataSet = getDsvDataSet();
                iterator = dsvDataSet.findAll().iterator();
            } catch (Exception e) {
                return rethrowExceptionAndReturn(e);
            }
        }
        return iterator;
    }

    private DsvDataSet getDsvDataSet() throws IOException {
        requireNonNull(getResource());
        return DsvDataSet.create(getResource());
    }
}
