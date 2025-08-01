package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class HDFSDataSet extends GelfDataSet {

    public HDFSDataSet(Resource resource) {
        super(resource);
        setName("HDFS");
    }

    @Override
    protected void update(GelfEvent event, Address sourceAddress, Address targetAddress, DsvRecord record) {
        event.add("pid", record.get("pid"));
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/HDFS/HDFS_2k.log_structured.csv")));
            setName("HDFS");
        }

        @Override
        public HDFSDataSet createDataSet() {
            return new HDFSDataSet(getResource());
        }
    }
}
