package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class ApacheDataSet extends GelfDataSet {

    public ApacheDataSet(Resource resource) {
        super(resource);
        setName("Apache HTTPd");
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Apache/Apache_2k.log_structured.csv")));
            setName("Apache HTTPd");
        }

        @Override
        public ApacheDataSet createDataSet() {
            return new ApacheDataSet(getResource());
        }
    }
}
