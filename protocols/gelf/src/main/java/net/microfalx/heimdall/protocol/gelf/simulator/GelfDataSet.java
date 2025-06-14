package net.microfalx.heimdall.protocol.gelf.simulator;

import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSet;
import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSetFactory;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.resource.Resource;

/**
 * A DSV based dataset for GELF (Graylog Extended Log Format) events.
 */
public class GelfDataSet extends DsvProtocolDataSet {

    public GelfDataSet(Resource resource) {
        super(resource);
    }

    /**
     * Invoked during simulator to update a GELF event based on past log data.
     *
     * @param event the event
     */
    public void update(GelfEvent event) {

    }

    public static abstract class Factory extends DsvProtocolDataSetFactory {

        public Factory(Resource resource) {
            super(resource);
        }

        @Override
        public abstract GelfDataSet createDataSet();
    }
}
