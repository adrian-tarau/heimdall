package net.microfalx.heimdall.rest.api;

import lombok.ToString;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;

/**
 * A group of functions/classes used in simulations.
 */
@ToString
public class Library extends NamedAndTaggedIdentifyAware<String> {

    public static final String PATH_SEPARATORS = ";:,";

    private Resource resource;
    private Project project;
    private String path;
    private Simulation.Type type;
    private Boolean override;
    private boolean global;

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
    public final String getPath() {
        return path;
    }

    /**
     * Returns the library code.
     *
     * @return a non-null instance
     */
    public final Resource getResource() {
        return resource;
    }

    /**
     * Returns the library project
     *
     * @return a non-null instance
     */
    public final Project getProject() {
        return project;
    }

    /**
     * Returns the type of simulation (the tool which will execute the simulation).
     *
     * @return a non-null instance
     */
    public final Simulation.Type getType() {
        return type;
    }

    /**
     * Returns whether the library (content) indicates an override (UI change).
     *
     * @return {@code true} if content override, {@code false} otherwise, {@code NULL} of override is undefined
     */
    public Boolean getOverride() {
        return override;
    }

    /**
     * Indicates whether the library is shared by all projects
     *
     * @return {@code true} if the library is shared by all projects, {@code false} otherwise
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Returns a new instance of this library with a different resource.
     *
     * @param resource the new resource
     * @return a new instance
     */
    public final Library withResource(Resource resource) {
        Library copy = (Library) copy();
        copy.resource = requireNonNull(resource);
        return copy;
    }

    /**
     * Returns a new instance of this library with a different override.
     *
     * @param override {@code true} if content override, {@code false} otherwise
     * @return a new instance
     */
    public final Library withOverride(Boolean override) {
        Library copy = (Library) copy();
        copy.override = override;
        return copy;
    }

    /**
     * Returns the natural identifier for a resource associated with a library.
     *
     * @param type      the simulation type
     * @param resource  the resource
     * @param projectId the project identifier, it can be null
     * @return a non-null instance
     */
    public static String getNaturalId(Simulation.Type type, Resource resource, String projectId) {
        requireNonNull(type);
        requireNonNull(resource);
        Hashing hashing = Hashing.create();
        hashing.update(projectId != null ? toIdentifier(projectId) : EMPTY_STRING);
        hashing.update(type.name().toLowerCase());
        hashing.update(resource.toHash());
        return hashing.asString();
    }


    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Resource resource;
        private Project project = Project.DEFAULT;
        private String path;
        private Simulation.Type type;
        private Boolean override;
        private boolean global;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder(Library library) {
            super(library.getId());
            this.tags(library.getTags()).name(library.getName()).description(library.getDescription());
            this.project(library.getProject()).type(library.getType()).path(library.getPath()).resource(library.getResource());
        }

        public final Builder resource(Resource resource) {
            requireNonNull(resource);
            this.resource = resource;
            if (emptyName()) this.name(resource.getName());
            if (isEmpty(path)) this.path = resource.getPath(true);
            return this;
        }

        public final Builder project(Project project) {
            requireNonNull(project);
            this.project = project;
            return this;
        }

        public final Builder type(Simulation.Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public final Builder path(String path) {
            requireNonNull(path);
            this.path = path;
            return this;
        }

        public final Builder override(Boolean override) {
            this.override = override;
            return this;
        }

        public final Builder global(boolean global) {
            this.global = global;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Library();
        }

        @Override
        protected final String updateId() {
            if (resource != null) {
                return Simulation.getNaturalId(type, resource, project != null ? project.getId() : null);
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
            library.override = override;
            library.global = global;
            return library;
        }
    }
}
