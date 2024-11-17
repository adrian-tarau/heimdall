package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * A group of functions/classes used in simulations.
 */
public class Library extends NamedAndTaggedIdentifyAware<String> {

    private Resource resource;
    private Project project;
    private String path;
    private Simulation.Type type;

    /**
     * Creates a library builder out of a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Builder create(Resource resource) {
        return new Builder().resource(resource);
    }

    /**
     * Returns the original path of the resource which supports this library.
     *
     * @return a non-null instance
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the library code.
     *
     * @return a non-null instance
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Returns the library project
     *
     * @return a non-null instance
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns the type of simulation (the tool which will execute the simulation).
     *
     * @return a non-null instance
     */
    public Simulation.Type getType() {
        return type;
    }


    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Resource resource;
        private Project project;
        private String path;
        private Simulation.Type type;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder resource(Resource resource) {
            requireNonNull(resource);
            this.resource = resource;
            if (emptyName()) this.name(resource.getName());
            if (isEmpty(path)) this.path = resource.getPath();
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder type(Simulation.Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder path(String path) {
            requireNonNull(path);
            this.path = path;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Library();
        }

        @Override
        protected String updateId() {
            if (resource != null) {
                return Simulation.getNaturalId(type, resource);
            } else {
                return super.updateId();
            }
        }

        @Override
        public Library build() {
            Library library = (Library) super.build();
            if (resource == null) throw new IllegalArgumentException("Library script is required");
            if (type == null) throw new IllegalArgumentException("The type is require");
            library.resource = resource;
            library.type = type;
            library.project = project;
            library.path = path;
            return library;
        }
    }
}
