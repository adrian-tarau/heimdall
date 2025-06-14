package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class ZookeeperDataSet extends GelfDataSet {

    public ZookeeperDataSet(Resource resource) {
        super(resource);
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Zookeeper/Zookeeper_2k.log_structured.csv")));
            setName("LogHub Zookeeper Data Set");
        }

        @Override
        public ZookeeperDataSet createDataSet() {
            return new ZookeeperDataSet(getResource());
        }
    }
}
