package io.fabrica.api.power;

import java.util.UUID;

/**
 * Base interface for any block that can interact with the power system.
 * Implement this interface to make a block participate in power networks.
 */
public interface IPowerCapable {

    /**
     * Gets the primary power capability of this block.
     *
     * @return the power capability type
     */
    PowerCapability getCapability();

    /**
     * Gets the position of this block in the world.
     *
     * @return the block position
     */
    BlockPos getPosition();

    /**
     * Gets the UUID of the power network this block belongs to.
     *
     * @return the network UUID, or null if not connected to any network
     */
    UUID getNetworkId();

    /**
     * Sets the network UUID for this block.
     * Called by the power system when networks are created, merged, or split.
     *
     * @param networkId the new network UUID, or null to disconnect
     */
    void setNetworkId(UUID networkId);

    /**
     * Called every tick while the block is part of a power network.
     * Use this for internal state updates.
     */
    default void onPowerTick() {
        // Default: no-op
    }

    /**
     * Checks if this block can connect to another power-capable block.
     * Override to implement custom connection rules.
     *
     * @param other the other power-capable block
     * @return true if connection is allowed
     */
    default boolean canConnectTo(IPowerCapable other) {
        return true;
    }

    /**
     * Gets a human-readable name for this power block.
     * Used in debug output and UIs.
     *
     * @return the display name
     */
    default String getDisplayName() {
        return getClass().getSimpleName();
    }
}
