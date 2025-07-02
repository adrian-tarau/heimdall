package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class OpenShiftDataSet extends GelfDataSet {

    public OpenShiftDataSet(Resource resource) {
        super(resource);
        setName("OpenShift");
    }

    @Override
    protected void update(GelfEvent event, Address sourceAddress, Address targetAddress, DsvRecord record) {
        event.add("pid", record.get("pid"));
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/OpenStack/OpenStack_2k.log_structured.csv")));
            setName("OpenShift");
        }

        @Override
        public OpenShiftDataSet createDataSet() {
            return new OpenShiftDataSet(getResource());
        }
    }
}
