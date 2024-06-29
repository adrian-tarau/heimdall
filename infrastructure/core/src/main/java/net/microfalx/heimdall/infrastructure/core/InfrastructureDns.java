package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.infrastructure.api.Dns;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.core.system.DnsRepository;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.bootstrap.core.utils.HostnameUtils.isHostname;
import static net.microfalx.bootstrap.core.utils.HostnameUtils.isIp;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.TimeUtils.millisSince;
import static net.microfalx.lang.TimeUtils.oneHourAgo;

public class InfrastructureDns extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureDns.class);

    private static final long DNS_UPDATE = TimeUtils.FIVE_MINUTE;

    private final Map<String, Dns> dnss = new ConcurrentHashMap<>();
    private final Map<String, Long> dnsLastUpdates = new ConcurrentHashMap<>();
    private final Map<String, InetAddress> addresses = new ConcurrentHashMap<>();
    private final Map<String, Long> addressesLastUpdates = new ConcurrentHashMap<>();

    private AsyncTaskExecutor executor;

    private final static InetAddress NA;

    void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    Dns getDns(Server server) {
        String id = getId(server);
        Dns dns = dnss.get(id);
        if (dns == null || shouldUpdateDns(id)) {
            Dns newDns = resolveDns(server);
            if (newDns.isValid()) {
                dnss.put(newDns.getId(), newDns);
                dns = newDns;
            }
        }
        return dns;
    }

    Server register(Server server) {
        doUpdate(server);
        Dns dns = getDns(server);
        if (isIp(server.getHostname()) && dns.isValid() && !dns.getFqdn().equalsIgnoreCase(server.getHostname())) {
            server = (Server) server.withHostname(dns.getFqdn()).withName(dns.getName());
        }
        return server;
    }

    void load() {
        try {
            List<net.microfalx.heimdall.infrastructure.core.system.Dns> dnsJpas = getBean(DnsRepository.class).findAll();
            for (net.microfalx.heimdall.infrastructure.core.system.Dns dnsJpa : dnsJpas) {
                Dns.Builder builder = new Dns.Builder(dnsJpa.getNaturalId());
                builder.domain(dnsJpa.getDomain()).hostname(dnsJpa.getHostname()).ip(dnsJpa.getIp()).valid(dnsJpa.isValid());
                builder.tags(CollectionUtils.setFromString(dnsJpa.getTags()))
                        .name(dnsJpa.getName()).description(dnsJpa.getDescription());
                Dns dns = builder.build();
                dnss.put(dns.getId(), dns);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load DNS", e);
        }
    }

    private String getId(Server server) {
        InetAddress address = resolveAddress(server);
        return StringUtils.toIdentifier(address.getHostAddress());
    }

    private InetAddress resolveAddress(Server server) {
        InetAddress address = addresses.get(server.getId());
        if (address == null) {
            AddressResolver addressResolver = new AddressResolver(server);
            addressResolver.run();
            address = addressResolver.address;
        } else if (executor != null && shouldUpdateAddress(server)) {
            executor.submit(new AddressResolver(server));
        }
        return address;
    }

    private Dns resolveDns(Server server) {
        InetAddress address = resolveAddress(server);
        String ip = address.getHostAddress();
        String hostname = address.getHostName();
        String canonicalHostname = address.getCanonicalHostName();
        String domain = null;
        if (isIp(hostname)) hostname = server.getHostname();
        if (isIp(canonicalHostname)) canonicalHostname = server.getHostname();
        if (!isIp(canonicalHostname)) {
            if (!canonicalHostname.equals(hostname) && canonicalHostname.length() > hostname.length()) {
                domain = canonicalHostname.substring(hostname.length() + 1);
            } else {
                int index = canonicalHostname.indexOf(".");
                if (index != -1) {
                    hostname = canonicalHostname.substring(0, index);
                    domain = canonicalHostname.substring(index + 1);
                }
            }
        }
        return (Dns) new Dns.Builder().ip(ip).hostname(hostname).domain(domain)
                .valid(isHostname(hostname)).tags(server.getTags()).name(server.getName()).build();
    }

    private void doUpdate(Server server) {
        Dns dns = resolveDns(server);
        NaturalIdEntityUpdater<net.microfalx.heimdall.infrastructure.core.system.Dns, Integer> entityUpdater = getUpdater(DnsRepository.class);
        net.microfalx.heimdall.infrastructure.core.system.Dns jpaDns = new net.microfalx.heimdall.infrastructure.core.system.Dns();
        jpaDns.setNaturalId(StringUtils.toIdentifier(dns.getIp()));
        jpaDns.setName(server.getName());
        jpaDns.setHostname(dns.getHostname());
        jpaDns.setDomain(dns.getDomain());
        jpaDns.setIp(dns.getIp());
        jpaDns.setTags(CollectionUtils.setToString(dns.getTags()));
        jpaDns.setValid(dns.isValid());
        entityUpdater.findByNaturalIdOrCreate(jpaDns);
    }

    private boolean shouldUpdateAddress(Server server) {
        return millisSince(addressesLastUpdates.getOrDefault(server.getId(), oneHourAgo())) > DNS_UPDATE;
    }

    private boolean shouldUpdateDns(String id) {
        return millisSince(dnsLastUpdates.getOrDefault(id, oneHourAgo())) > DNS_UPDATE;
    }

    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }

    private class AddressResolver implements Runnable {

        private final Server server;
        private InetAddress address;

        public AddressResolver(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                address = InetAddress.getByName(server.getHostname());
                address.getCanonicalHostName();
            } catch (UnknownHostException e) {
                address = NA;
            } finally {
                addressesLastUpdates.put(toIdentifier(server.getHostname()), currentTimeMillis());
            }
        }
    }

    static {
        try {
            NA = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


}
