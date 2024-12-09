package net.microfalx.heimdall.infrastructure.ping.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.ping.PingHealth;
import net.microfalx.heimdall.infrastructure.ping.PingService;
import net.microfalx.lang.annotation.Provider;

import java.util.ArrayList;
import java.util.List;

@Provider
public class PingDataSet extends MemoryDataSet<Ping, PojoField<Ping>, String> {

    public PingDataSet(DataSetFactory<Ping, PojoField<Ping>, String> factory, Metadata<Ping, PojoField<Ping>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Ping> extractModels(Filter filterable) {
        PingHealth pingHealth = getService(PingHealth.class);
        PingService pingService = getService(PingService.class);
        List<Ping> pings = new ArrayList<>();
        pingHealth.getLastPings().forEach((ping) -> {
            Ping overViewPing = new Ping();
            overViewPing.setId(ping.getId());
            overViewPing.setName(ping.getName());
            overViewPing.setDescription(ping.getDescription());
            overViewPing.setService(ping.getService());
            overViewPing.setServer(ping.getServer());
            overViewPing.setHealth(pingService.getHealth(overViewPing.getService(), overViewPing.getServer()));
            overViewPing.setStatus(pingService.getStatus(overViewPing.getService(), overViewPing.getServer()));
            overViewPing.setLastDuration(pingHealth.getLastDuration(overViewPing.getService(), overViewPing.getServer()));
            overViewPing.setMinimumDuration(pingHealth.getMinDuration(overViewPing.getService(), overViewPing.getServer()));
            overViewPing.setMaximumDuration(pingHealth.getMaxDuration(overViewPing.getService(), overViewPing.getServer()));
            overViewPing.setAverageDuration(pingHealth.getAverageDuration(overViewPing.getService(), overViewPing.getServer()));
            pings.add(overViewPing);
        });
        return pings;
    }
}
