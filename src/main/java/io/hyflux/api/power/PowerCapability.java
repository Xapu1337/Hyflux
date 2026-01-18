package io.fabrica.api.power;

/**
 * Enum defining the different power capabilities a block can have.
 */
public enum PowerCapability {
    /**
     * Block produces power (generators, solar panels).
     */
    PRODUCER,

    /**
     * Block consumes power (machines).
     */
    CONSUMER,

    /**
     * Block stores power (batteries, capacitors).
     */
    STORAGE,

    /**
     * Block transmits power (cables, wires).
     */
    CONDUIT,

    /**
     * Block has no power capability.
     */
    NONE
}
