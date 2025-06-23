package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.metrics.Metrics;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.MOFilter;
import org.snmp4j.agent.mo.lock.LockRequest;
import org.snmp4j.agent.request.Request;
import org.snmp4j.smi.OctetString;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import static net.microfalx.heimdall.protocol.snmp.SnmpUtils.describeScope;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An implementation of {@link MOServer} that tracks lookups and provides metrics.
 */
class AgentMoServer implements MOServer {

    private static final Metrics MO_METRICS = SnmpUtils.METRICS.withGroup("Managed Objects");
    private static final Metrics LOOKUP_METRICS = MO_METRICS.withGroup("Lookup");

    private final MOServer delegate;

    AgentMoServer(MOServer delegate) {
        requireNonNull(delegate);
        this.delegate = delegate;
    }

    @Override
    public void addContext(OctetString context) {
        delegate.addContext(context);
    }

    @Override
    public void removeContext(OctetString context) {
        delegate.removeContext(context);
    }

    @Override
    public boolean isContextSupported(OctetString context) {
        return delegate.isContextSupported(context);
    }

    @Override
    public void addContextListener(ContextListener l) {
        delegate.addContextListener(l);
    }

    @Override
    public void removeContextListener(ContextListener l) {
        delegate.removeContextListener(l);
    }

    @Override
    public void register(ManagedObject mo, OctetString context) throws DuplicateRegistrationException {
        delegate.register(mo, context);
    }

    @Override
    public ManagedObject<?> unregister(ManagedObject mo, OctetString context) {
        return delegate.unregister(mo, context);
    }

    @Override
    public ManagedObject<?> lookup(MOQuery query) {
        return delegate.lookup(query);
    }

    @Override
    public <MO extends ManagedObject<?>> MO lookup(MOQuery query, Class<MO> managedObjectType) {
        trackLookup(query);
        return delegate.lookup(query, managedObjectType);
    }

    @Override
    public ManagedObject<?> lookup(MOQuery query, LockRequest lockRequest) {
        trackLookup(query);
        return delegate.lookup(query, lockRequest);
    }

    @Override
    public ManagedObject<?> lookup(MOQuery query, LockRequest lockRequest, MOServerLookupEvent lookupEvent) {
        trackLookup(query);
        return delegate.lookup(query, lockRequest, lookupEvent);
    }

    @Override
    public OctetString[] getContexts() {
        return delegate.getContexts();
    }

    @Override
    public Iterator<Map.Entry<MOScope, ManagedObject<?>>> iterator(Comparator<MOScope> comparator, MOFilter moFilter) {
        return delegate.iterator(comparator, moFilter);
    }

    @Override
    public void addLookupListener(MOServerLookupListener listener, ManagedObject<?> mo) {
        delegate.addLookupListener(listener, mo);
    }

    @Override
    public boolean removeLookupListener(MOServerLookupListener listener, ManagedObject<?> mo) {
        return delegate.removeLookupListener(listener, mo);
    }

    @Override
    public <MO extends ManagedObject<?>> MO lookup(MOQuery query, LockRequest lockRequest, MOServerLookupEvent lookupEvent, Class<MO> managedObjectType) {
        trackLookup(query);
        return delegate.lookup(query, lockRequest, lookupEvent, managedObjectType);
    }

    @Override
    public Iterator<Map.Entry<MOScope, ManagedObject<?>>> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean lock(Object owner, ManagedObject<?> managedObject) {
        return delegate.lock(owner, managedObject);
    }

    @Override
    public boolean lock(Object owner, ManagedObject<?> managedObject, long timeoutMillis) {
        return delegate.lock(owner, managedObject, timeoutMillis);
    }

    @Override
    public boolean unlock(Object owner, ManagedObject<?> managedObject) {
        return delegate.unlock(owner, managedObject);
    }

    @Override
    public boolean waitForUnlockedState(long waitTimeoutMillis) {
        return delegate.waitForUnlockedState(waitTimeoutMillis);
    }

    @Override
    public boolean unlockNow(Object owner, ManagedObject<?> managedObject) {
        return delegate.unlockNow(owner, managedObject);
    }

    @Override
    public OctetString[] getRegisteredContexts(ManagedObject<?> managedObject) {
        return delegate.getRegisteredContexts(managedObject);
    }

    @Override
    public Map<OctetString, MOScope> getRegisteredScopes(ManagedObject<?> managedObject) {
        return delegate.getRegisteredScopes(managedObject);
    }

    @Override
    public boolean registerNew(ManagedObject<?> mo, OctetString context) {
        return delegate.registerNew(mo, context);
    }

    @Override
    public Iterator<Map.Entry<MOScope, ManagedObject<?>>> iterator(Comparator<MOScope> comparator) {
        return delegate.iterator(comparator);
    }

    @Override
    public String toString() {
        return "AgentMoServer{" + "delegate=" + delegate + '}';
    }

    private void trackLookup(MOQuery query) {
        String queryDescription = getSource(query) + ": " + describeScope(query);
        LOOKUP_METRICS.count(queryDescription);
    }

    private String getSource(MOQuery query) {
        if (query instanceof DefaultMOQuery defaultQuery) {
            Object source = defaultQuery.getSource();
            if (source instanceof Request<?, ?, ?>) {
                return "Request";
            }
        }
        return "Unknown Source";
    }


}
