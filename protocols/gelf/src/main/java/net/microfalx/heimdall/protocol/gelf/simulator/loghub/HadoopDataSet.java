package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfDataSet;
import net.microfalx.lang.UriUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

public class HadoopDataSet extends GelfDataSet {

    public HadoopDataSet(Resource resource) {
        super(resource);
        setName("Hadoop");
    }

    @Override
    protected void update(GelfEvent event, Address sourceAddress, Address targetAddress, DsvRecord record) {
        super.update(event, sourceAddress, targetAddress);
        event.setProcess(record.get("process"));
    }

    @Provider
    public static class Factory extends GelfDataSet.Factory {

        public Factory() {
            super(Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/logpai/loghub/refs/heads/master/Hadoop/Hadoop_2k.log_structured.csv")));
            setName("Hadoop");
        }

        @Override
        public HadoopDataSet createDataSet() {
            return new HadoopDataSet(getResource());
        }
    }
}
