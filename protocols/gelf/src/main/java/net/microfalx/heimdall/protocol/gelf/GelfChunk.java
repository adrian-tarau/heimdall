package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds a GELF chunk received over UDP.
 */
class GelfChunk implements Comparable<GelfChunk> {

    public static final short MARKER = 0x1e0f;
    private int index;
    private Resource resource;

    public GelfChunk(int index, Resource resource) {
        requireNonNull(resource);
        this.index = index;
        this.resource = resource;
    }

    public int getIndex() {
        return index;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "GelfChunk{" +
                "index=" + index +
                ", resource=" + resource +
                '}';
    }

    @Override
    public int compareTo(GelfChunk o) {
        return Integer.compare(getIndex(),o.getIndex());
    }
}
