package io.fabrica.api.power;

import java.util.List;

/**
 * Listener interface for power network events.
 * Other mods can implement this to react to network changes.
 */
public interface PowerNetworkListener {

    /**
     * Called when a new power network is created.
     *
     * @param network the newly created network
     */
    default void onNetworkCreated(PowerNetworkSnapshot network) {}

    /**
     * Called when two networks are merged into one.
     *
     * @param sourceId the UUID of the network being merged (will be dissolved)
     * @param targetId the UUID of the network receiving the merge
     */
    default void onNetworkMerged(java.util.UUID sourceId, java.util.UUID targetId) {}

    /**
     * Called when a network is split into multiple networks.
     *
     * @param originalId the UUID of the original network
     * @param fragmentIds the UUIDs of the resulting fragment networks
     */
    default void onNetworkSplit(java.util.UUID originalId, List<java.util.UUID> fragmentIds) {}

    /**
     * Called when a power-capable block is added to a network.
     *
     * @param capable the block that was added
     * @param networkId the network it was added to
     */
    default void onCapableAdded(IPowerCapable capable, java.util.UUID networkId) {}

    /**
     * Called when a power-capable block is removed from a network.
     *
     * @param capable the block that was removed
     * @param networkId the network it was removed from
     */
    default void onCapableRemoved(IPowerCapable capable, java.util.UUID networkId) {}

    /**
     * Called every tick with updated network statistics.
     *
     * @param networkId the network UUID
     * @param stats the current network statistics
     */
    default void onNetworkTick(java.util.UUID networkId, PowerNetworkStats stats) {}
}
