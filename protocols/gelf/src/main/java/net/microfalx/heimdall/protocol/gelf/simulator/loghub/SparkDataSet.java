package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class SparkDataSet extends GelfDataSet {
    public SparkDataSet(Resource resource) {
        super(resource);
    }

    @Override
    protected void update(GelfEvent event, Address sourceAddress, Address targetAddress) {
        DsvRecord record = iterator().next();
        event.setSource(sourceAddress);
        event.addTarget(targetAddress);
        event.setBody(Body.create(record.get("content")));
        event.setLogger(record.get("component"));
        event.setGelfSeverity(getSeverity(record.get("level")));
        event.add("correlationalId", getCorrelationalId(record.get("EventId"), record.get("EventTemplate")));
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Spark/Spark_2k.log_structured.csv")));
            setName("LogHub Spark Data Set");
        }

        @Override
        public SparkDataSet createDataSet() {
            return new SparkDataSet(getResource());
        }
    }
}
