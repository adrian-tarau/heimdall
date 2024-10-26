package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;

/**
 * A group of functions/classes used in simulations.
 */
public class Library extends NamedAndTaggedIdentifyAware<String> {

    private Resource resource;

    /**
     * Returns the library code.
     *
     * @return a non-null instance
     */
    public Resource getResource() {
        return resource;
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Resource resource;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder resource(Resource resource) {
            this.resource = resource;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Library();
        }

        @Override
        public NamedAndTaggedIdentifyAware<String> build() {
            Library library = (Library) super.build();
            if (resource == null) throw new IllegalArgumentException("Library script is required");
            library.resource = resource;
            return library;
        }
    }
}
