package io.fabrica.api.power;

import java.util.Set;
import java.util.UUID;

/**
 * An immutable snapshot of a power network's state.
 * Used for querying network information without exposing internal implementation.
 */
public final class PowerNetworkSnapshot {

    private final UUID networkId;
    private final Set<BlockPos> memberPositions;
    private final int producerCount;
    private final int consumerCount;
    private final int storageCount;
    private final int conduitCount;
    private final double totalProductionRate;
    private final double totalConsumptionRate;
    private final double totalStoredEnergy;
    private final double totalStorageCapacity;

    public PowerNetworkSnapshot(
            UUID networkId,
            Set<BlockPos> memberPositions,
            int producerCount,
            int consumerCount,
            int storageCount,
            int conduitCount,
            double totalProductionRate,
            double totalConsumptionRate,
            double totalStoredEnergy,
            double totalStorageCapacity) {
        this.networkId = networkId;
        this.memberPositions = Set.copyOf(memberPositions);
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
        this.storageCount = storageCount;
        this.conduitCount = conduitCount;
        this.totalProductionRate = totalProductionRate;
        this.totalConsumptionRate = totalConsumptionRate;
        this.totalStoredEnergy = totalStoredEnergy;
        this.totalStorageCapacity = totalStorageCapacity;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public Set<BlockPos> getMemberPositions() {
        return memberPositions;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public int getStorageCount() {
        return storageCount;
    }

    public int getConduitCount() {
        return conduitCount;
    }

    public int getTotalMemberCount() {
        return producerCount + consumerCount + storageCount + conduitCount;
    }

    /**
     * Gets the total production rate of all producers in the network.
     *
     * @return total production in Watts (W)
     */
    public double getTotalProductionRate() {
        return totalProductionRate;
    }

    /**
     * Gets the total consumption rate of all consumers in the network.
     *
     * @return total consumption in Watts (W)
     */
    public double getTotalConsumptionRate() {
        return totalConsumptionRate;
    }

    /**
     * Gets the total stored energy across all storage blocks.
     *
     * @return total stored energy in Joules (J)
     */
    public double getTotalStoredEnergy() {
        return totalStoredEnergy;
    }

    /**
     * Gets the total storage capacity across all storage blocks.
     *
     * @return total capacity in Joules (J)
     */
    public double getTotalStorageCapacity() {
        return totalStorageCapacity;
    }

    /**
     * Gets the net power balance (production - consumption).
     * Positive means surplus, negative means deficit.
     *
     * @return net power in Watts (W)
     */
    public double getNetPowerBalance() {
        return totalProductionRate - totalConsumptionRate;
    }

    /**
     * Calculates how long the stored energy would last at current consumption
     * (assuming no production).
     *
     * @return time in seconds, or Double.POSITIVE_INFINITY if no consumption
     */
    public double getStorageRuntime() {
        if (totalConsumptionRate <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return totalStoredEnergy / totalConsumptionRate;
    }

    @Override
    public String toString() {
        return String.format(
            "PowerNetwork[id=%s, production=%.1fW, consumption=%.1fW, stored=%.1fJ/%.1fJ]",
            networkId.toString().substring(0, 8),
            totalProductionRate,
            totalConsumptionRate,
            totalStoredEnergy,
            totalStorageCapacity
        );
    }
}
