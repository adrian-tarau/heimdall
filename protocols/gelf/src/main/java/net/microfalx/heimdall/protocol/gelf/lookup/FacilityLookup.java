package net.microfalx.heimdall.protocol.gelf.lookup;

import com.cloudbees.syslog.Facility;
import net.microfalx.bootstrap.dataset.AbstractLookup;
import net.microfalx.bootstrap.dataset.AbstractLookupProvider;
import net.microfalx.lang.annotation.Provider;

import java.util.Arrays;

import static net.microfalx.lang.StringUtils.capitalizeWords;

public class FacilityLookup extends AbstractLookup<Integer> {
    public FacilityLookup(Integer id, String name) {
        super(id, name);
    }

    @Provider
    public static class FacilityLookupProvider extends AbstractLookupProvider<FacilityLookup> {

        @Override
        public Iterable<FacilityLookup> extractAll() {
            return Arrays.stream(Facility.values()).map(s -> new FacilityLookup(s.numericalCode(), capitalizeWords(s.label()))).toList();
        }
    }
}
