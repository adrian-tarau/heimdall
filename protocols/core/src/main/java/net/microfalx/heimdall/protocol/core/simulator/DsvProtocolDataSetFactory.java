package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.dsv.DsvField;
import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.resource.Resource;

/**
 * Base class for DSV (Delimited Separated Values) protocol data set factories.
 */
public abstract class DsvProtocolDataSetFactory extends AbstractProtocolDataSetFactory<DsvRecord, DsvField, String> {

    public DsvProtocolDataSetFactory(Resource resource) {
        super(resource);
    }
}
