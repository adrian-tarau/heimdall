package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

/**
 * A single step within a scenario.
 */
public class Step extends NamedAndTaggedIdentifyAware<String> {

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        @Override
        protected IdentityAware<String> create() {
            return new Simulation();
        }

        @Override
        public Step build() {
            Step step = (Step) super.build();
            return step;
        }
    }
}
