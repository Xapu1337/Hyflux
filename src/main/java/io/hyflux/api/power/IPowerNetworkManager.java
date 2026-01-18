package io.fabrica.api.power;

import java.util.Collection;
import java.util.UUID;

/**
 * Public API for managing power networks.
 * Other mods can use this interface to query and interact with the power system.
 *
 * <p>Access via: {@code FabricaAPI.getPowerManager()}
 */
public interface IPowerNetworkManager {

    // ==================== Network Operations ====================

    /**
     * Gets a snapshot of a network by its UUID.
     *
     * @param networkId the network UUID
     * @return network snapshot, or null if not found
     */
    PowerNetworkSnapshot getNetwork(UUID networkId);

    /**
     * Gets the network that contains the block at the given position.
     *
     * @param pos the block position
     * @return network snapshot, or null if no power block at that position
     */
    PowerNetworkSnapshot getNetworkAt(BlockPos pos);

    /**
     * Gets all active power networks.
     *
     * @return collection of network snapshots
     */
    Collection<PowerNetworkSnapshot> getAllNetworks();

    /**
     * Registers a power-capable block with the system.
     * This will either add it to an existing network or create a new one.
     *
     * @param capable the power-capable block to register
     */
    void registerCapable(IPowerCapable capable);

    /**
     * Unregisters a power-capable block from the system.
     * This may cause a network to split or be destroyed.
     *
     * @param capable the power-capable block to unregister
     */
    void unregisterCapable(IPowerCapable capable);

    /**
     * Gets the power-capable block at a position, if any.
     *
     * @param pos the position to check
     * @return the power-capable block, or null if none
     */
    IPowerCapable getCapableAt(BlockPos pos);

    // ==================== Query Methods ====================

    /**
     * Gets the total power production of a network.
     *
     * @param networkId the network UUID
     * @return total production in Watts (W), or 0 if network not found
     */
    double getNetworkProduction(UUID networkId);

    /**
     * Gets the total power consumption of a network.
     *
     * @param networkId the network UUID
     * @return total consumption in Watts (W), or 0 if network not found
     */
    double getNetworkConsumption(UUID networkId);

    /**
     * Gets the total stored energy in a network.
     *
     * @param networkId the network UUID
     * @return total stored energy in Joules (J), or 0 if network not found
     */
    double getNetworkStorage(UUID networkId);

    /**
     * Gets the total storage capacity of a network.
     *
     * @param networkId the network UUID
     * @return total capacity in Joules (J), or 0 if network not found
     */
    double getNetworkCapacity(UUID networkId);

    /**
     * Gets the current statistics for a network's last tick.
     *
     * @param networkId the network UUID
     * @return network statistics, or null if network not found
     */
    PowerNetworkStats getNetworkStats(UUID networkId);

    // ==================== Event Listeners ====================

    /**
     * Adds a listener for power network events.
     *
     * @param listener the listener to add
     */
    void addListener(PowerNetworkListener listener);

    /**
     * Removes a listener for power network events.
     *
     * @param listener the listener to remove
     */
    void removeListener(PowerNetworkListener listener);

    // ==================== Utility Methods ====================

    /**
     * Converts Watts to Joules per tick.
     * At 20 ticks/second: 1 W = 0.05 J/tick
     *
     * @param watts power in Watts
     * @return energy per tick in Joules
     */
    static double wattsToJoulesPerTick(double watts) {
        return watts / 20.0;
    }

    /**
     * Converts Joules per tick to Watts.
     * At 20 ticks/second: 1 J/tick = 20 W
     *
     * @param joulesPerTick energy per tick in Joules
     * @return power in Watts
     */
    static double joulesPerTickToWatts(double joulesPerTick) {
        return joulesPerTick * 20.0;
    }

    /**
     * Formats a power value for display.
     *
     * @param watts power in Watts
     * @return formatted string (e.g., "100 W", "1.5 kW", "2.3 MW")
     */
    static String formatPower(double watts) {
        if (watts >= 1_000_000) {
            return String.format("%.2f MW", watts / 1_000_000);
        } else if (watts >= 1_000) {
            return String.format("%.2f kW", watts / 1_000);
        } else {
            return String.format("%.1f W", watts);
        }
    }

    /**
     * Formats an energy value for display.
     *
     * @param joules energy in Joules
     * @return formatted string (e.g., "100 J", "1.5 kJ", "2.3 MJ")
     */
    static String formatEnergy(double joules) {
        if (joules >= 1_000_000) {
            return String.format("%.2f MJ", joules / 1_000_000);
        } else if (joules >= 1_000) {
            return String.format("%.2f kJ", joules / 1_000);
        } else {
            return String.format("%.1f J", joules);
        }
    }
}
