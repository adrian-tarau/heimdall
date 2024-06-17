package net.microfalx.heimdall.infrastructure.api;

import net.microfalx.lang.IdentifiableNameAware;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.StringUtils;

public class Dns extends IdentifiableNameAware<String> {

    private String hostname;
    private String domain;
    private String ip;

    /**
     * A builder class.
     */
    public static class Builder extends IdentifiableNameAware.Builder<String> {

        private String hostname;
        private String domain;
        private String ip;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Dns();
        }

        @Override
        protected String updateId() {
            return StringUtils.toIdentifier(ip);
        }

        @Override
        public Dns build() {
            Dns dns = (Dns) super.build();
            dns.ip = ip;
            dns.hostname = hostname;
            dns.domain = domain;
            return dns;
        }
    }
}
