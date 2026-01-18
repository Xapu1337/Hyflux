package io.fabrica.api.power;

/**
 * Interface for blocks that store power (batteries, capacitors, etc.).
 * Storage blocks buffer energy between production and consumption.
 */
public interface IPowerStorage extends IPowerCapable {

    /**
     * Gets the current stored energy.
     *
     * @return stored energy in Joules (J)
     */
    double getStoredEnergy();

    /**
     * Gets the maximum storage capacity.
     *
     * @return capacity in Joules (J)
     */
    double getMaxCapacity();

    /**
     * Gets the maximum charge rate (how fast energy can flow in).
     *
     * @return max charge rate in Watts (W)
     */
    double getMaxChargeRate();

    /**
     * Gets the maximum discharge rate (how fast energy can flow out).
     *
     * @return max discharge rate in Watts (W)
     */
    double getMaxDischargeRate();

    /**
     * Attempts to charge this storage with the given energy.
     *
     * @param joules energy to add in Joules (J)
     * @return overflow energy that couldn't be stored (J)
     */
    double charge(double joules);

    /**
     * Attempts to discharge energy from this storage.
     *
     * @param joules energy to extract in Joules (J)
     * @return actual energy extracted in Joules (J)
     */
    double discharge(double joules);

    /**
     * Gets the current charge level as a percentage (0.0 to 1.0).
     *
     * @return charge percentage
     */
    default double getChargePercentage() {
        double max = getMaxCapacity();
        return max > 0 ? getStoredEnergy() / max : 0.0;
    }

    /**
     * Checks if this storage is fully charged.
     *
     * @return true if at max capacity
     */
    default boolean isFull() {
        return getStoredEnergy() >= getMaxCapacity();
    }

    /**
     * Checks if this storage is empty.
     *
     * @return true if no energy stored
     */
    default boolean isEmpty() {
        return getStoredEnergy() <= 0;
    }

    @Override
    default PowerCapability getCapability() {
        return PowerCapability.STORAGE;
    }
}
