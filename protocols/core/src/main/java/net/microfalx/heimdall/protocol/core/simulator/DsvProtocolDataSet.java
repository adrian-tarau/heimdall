package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.dsv.DsvField;
import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.resource.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class DsvProtocolDataSet extends AbstractProtocolDataSet<DsvRecord, DsvField, String> {

    public DsvProtocolDataSet(Resource resource) {
        super(resource);
    }

    @Override
    public Metadata<DsvRecord, DsvField, String> getMetadata() {
        return null;
    }

    @Override
    public @NotNull Iterator<DsvRecord> iterator() {
        return null;
    }
}
