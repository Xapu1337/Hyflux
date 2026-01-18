package io.fabrica.api.power;

/**
 * Interface for blocks that consume power (machines, lights, etc.).
 * Consumers draw energy from the network to operate.
 */
public interface IPowerConsumer extends IPowerCapable {

    /**
     * Gets the power consumption rate when this consumer is operating.
     *
     * @return consumption in Watts (W)
     */
    double getConsumptionRate();

    /**
     * Called by the power system to provide energy to this consumer.
     *
     * @param availableJoules energy available in Joules (J)
     * @return actual energy consumed in Joules (J)
     */
    double consume(double availableJoules);

    /**
     * Checks if this consumer can currently operate.
     * Returns false if there's not enough power or the machine is idle.
     *
     * @return true if the consumer is operating
     */
    boolean canOperate();

    /**
     * Checks if this consumer is currently requesting power.
     * A machine might not request power if it has no work to do.
     *
     * @return true if power is needed
     */
    default boolean isRequestingPower() {
        return canOperate();
    }

    /**
     * Gets the internal energy buffer of this consumer.
     * Machines may buffer energy for operations that require bursts.
     *
     * @return buffered energy in Joules (J)
     */
    default double getEnergyBuffer() {
        return 0.0;
    }

    /**
     * Gets the maximum internal energy buffer capacity.
     *
     * @return buffer capacity in Joules (J)
     */
    default double getMaxEnergyBuffer() {
        return 0.0;
    }

    @Override
    default PowerCapability getCapability() {
        return PowerCapability.CONSUMER;
    }
}
