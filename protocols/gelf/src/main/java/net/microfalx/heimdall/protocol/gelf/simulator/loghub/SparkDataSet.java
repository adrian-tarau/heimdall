package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class SparkDataSet extends GelfDataSet {

    public SparkDataSet(Resource resource) {
        super(resource);
        setName("Spark");
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Spark/Spark_2k.log_structured.csv")));
            setName("Spark");
        }

        @Override
        public SparkDataSet createDataSet() {
            return new SparkDataSet(getResource());
        }
    }
}
