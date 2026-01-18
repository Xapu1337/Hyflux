package io.fabrica.api.power;

import java.util.Set;

/**
 * Interface for blocks that conduct power between components (cables, wires).
 *
 * <p>In the MVP, conduits have unlimited throughput and no energy loss.
 * Future versions may implement voltage tiers and resistance-based losses.
 */
public interface IPowerConduit extends IPowerCapable {

    /**
     * Gets the maximum power throughput of this conduit.
     * In the MVP this is unlimited (Double.MAX_VALUE).
     *
     * @return maximum throughput in Watts (W)
     */
    double getMaxThroughput();

    /**
     * Gets all positions that this conduit connects to.
     * This includes adjacent power-capable blocks and other conduits.
     *
     * @return set of connected block positions
     */
    Set<BlockPos> getConnectedPositions();

    /**
     * Checks if this conduit can connect to a block at the given position.
     *
     * @param pos the position to check
     * @return true if connection is possible
     */
    default boolean canConnectTo(BlockPos pos) {
        return getPosition().isAdjacent(pos);
    }

    @Override
    default PowerCapability getCapability() {
        return PowerCapability.CONDUIT;
    }
}
