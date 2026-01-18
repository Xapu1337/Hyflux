package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.IPowerCapable;
import io.fabrica.api.power.IPowerConduit;
import io.fabrica.api.power.PowerCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Power cable that connects machines into power networks.
 *
 * MVP specs:
 * - Unlimited throughput (no cable loss)
 * - Auto-connects to adjacent power blocks
 */
public class CableBlock extends MachineBlock implements IPowerConduit {

    private final Set<BlockPos> connectedPositions;

    public CableBlock(BlockPos position) {
        super("cable", position);
        this.connectedPositions = new HashSet<>();
    }

    @Override
    public PowerCapability getCapability() {
        return PowerCapability.CONDUIT;
    }

    @Override
    public double getMaxThroughput() {
        // MVP: unlimited throughput
        return Double.MAX_VALUE;
    }

    @Override
    public Set<BlockPos> getConnectedPositions() {
        return new HashSet<>(connectedPositions);
    }

    /**
     * Updates the list of connected positions.
     * Should be called when the cable is placed or when adjacent blocks change.
     *
     * @param registry the machine registry to check for adjacent machines
     */
    public void updateConnections(MachineRegistry registry) {
        connectedPositions.clear();

        for (BlockPos adjacent : position.getAdjacent()) {
            MachineBlock machine = registry.getMachineAt(adjacent);
            if (machine != null && machine instanceof IPowerCapable) {
                connectedPositions.add(adjacent);
            }
        }
    }

    /**
     * Adds a connection to another block.
     */
    public void addConnection(BlockPos pos) {
        if (position.isAdjacent(pos)) {
            connectedPositions.add(pos);
        }
    }

    /**
     * Removes a connection to another block.
     */
    public void removeConnection(BlockPos pos) {
        connectedPositions.remove(pos);
    }

    /**
     * Gets the number of connections.
     */
    public int getConnectionCount() {
        return connectedPositions.size();
    }

    @Override
    public void tick() {
        // Cables don't need to tick in MVP
        // Future: could check for damage, heat, etc.
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Cables don't have a GUI in MVP
        // Future: could show power flow information
    }

    @Override
    public void onPlace() {
        super.onPlace();
        updateConnections(MachineRegistry.getInstance());
    }

    @Override
    public String getMachineTypeName() {
        return "Cable";
    }

    @Override
    public String getMachineTypeId() {
        return "cable";
    }
}
