package net.microfalx.heimdall.protocol.gelf.lookup;

import com.cloudbees.syslog.Severity;
import net.microfalx.bootstrap.dataset.AbstractLookup;
import net.microfalx.bootstrap.dataset.AbstractLookupProvider;
import net.microfalx.lang.annotation.Provider;

import java.util.Arrays;

import static net.microfalx.lang.StringUtils.capitalizeWords;

public class SeverityLookup extends AbstractLookup<Integer> {

    public SeverityLookup(Integer id, String name) {
        super(id, name);
    }

    @Provider
    public static class SeverityLookupProvider extends AbstractLookupProvider<SeverityLookup> {

        @Override
        public Iterable<SeverityLookup> extractAll() {
            return Arrays.stream(Severity.values()).map(s -> new SeverityLookup(s.numericalCode(), capitalizeWords(s.label()))).toList();
        }
    }
}
