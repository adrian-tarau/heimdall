package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class ZookeeperDataSet extends GelfDataSet {

    public ZookeeperDataSet(Resource resource) {
        super(resource);
        setName("Zookeeper");
    }

    @Override
    public void update(GelfEvent event, Address sourceAddress, Address targetAddress, DsvRecord record) {
        event.setProcess(record.get("node"));
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Zookeeper/Zookeeper_2k.log_structured.csv")));
            setName("Zookeeper");
        }

        @Override
        public ZookeeperDataSet createDataSet() {
            return new ZookeeperDataSet(getResource());
        }
    }
}
