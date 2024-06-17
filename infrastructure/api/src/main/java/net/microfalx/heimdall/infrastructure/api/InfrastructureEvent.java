package net.microfalx.heimdall.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * An event for an infrastructure change.
 */
@Getter
@ToString
@AllArgsConstructor
public class InfrastructureEvent {

    private final InfrastructureElement element;
    private final Type type;

    /**
     * An enum for the type of event.
     */
    enum Type {

        /**
         * An infrastructure element was added
         */
        ADD,

        /**
         * An infrastructure element was changed.
         */
        CHANGE,

        /**
         * An infrastructure element was removed
         */
        REMOVE
    }

}
