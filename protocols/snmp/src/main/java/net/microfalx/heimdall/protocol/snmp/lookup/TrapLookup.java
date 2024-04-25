package net.microfalx.heimdall.protocol.snmp.lookup;

import net.microfalx.bootstrap.dataset.AbstractLookup;
import net.microfalx.bootstrap.dataset.AbstractLookupProvider;
import net.microfalx.lang.annotation.Provider;
import org.snmp4j.PDU;

import static java.util.stream.Stream.of;
import static net.microfalx.lang.StringUtils.capitalizeWords;
import static org.snmp4j.PDU.getTypeString;

public class TrapLookup extends AbstractLookup<Integer> {

    public TrapLookup(Integer id, String name) {
        super(id, name);
    }

    @Provider
    public static class FacilityLookupProvider extends AbstractLookupProvider<TrapLookup, Integer> {

        @Override
        public Iterable<TrapLookup> doFindAll() {
            return of(PDU.TRAP, PDU.V1TRAP).
                    map(t -> new TrapLookup(t, capitalizeWords(getTypeString(t)))).toList();
        }
    }
}
