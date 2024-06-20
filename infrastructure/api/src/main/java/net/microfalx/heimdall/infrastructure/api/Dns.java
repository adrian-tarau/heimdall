package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;

@ToString
public class Dns extends NamedAndTaggedIdentifyAware<String> {

    private String hostname;
    private String domain;
    private String ip;
    private boolean valid;

    /**
     * Returns the IP.
     *
     * @return a non-null instance
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns the hostname.
     *
     * @return the hostname, IP if a hostname is not available
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the domain.
     *
     * @return the domain, null if a domain is not available
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the fully qualified domain name.
     *
     * @return a non-null instance
     */
    public String getFqdn() {
        if (StringUtils.isNotEmpty(domain)) {
            return hostname + "." + domain;
        } else {
            return getHostname();
        }
    }

    /**
     * Returns whether this DNS entry is valid - has a hostname and/or domain.
     *
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private String hostname;
        private String domain;
        private String ip;
        private boolean valid;

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

        public Builder valid(boolean valid) {
            this.valid = valid;
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
            dns.valid = valid;
            return dns;
        }
    }
}
