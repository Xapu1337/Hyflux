package io.fabrica.api.power;

/**
 * Interface for blocks that produce power (generators, solar panels, etc.).
 * Producers generate energy that flows into the network.
 */
public interface IPowerProducer extends IPowerCapable {

    /**
     * Gets the maximum power production rate of this producer.
     *
     * @return maximum production in Watts (W)
     */
    double getMaxProductionRate();

    /**
     * Gets the current power production rate.
     * May be less than max if fuel is low, daylight conditions, etc.
     *
     * @return current production in Watts (W)
     */
    double getCurrentProductionRate();

    /**
     * Called by the power system to extract energy from this producer.
     *
     * @param maxJoules maximum energy to extract in Joules (J)
     * @return actual energy extracted in Joules (J)
     */
    double produce(double maxJoules);

    /**
     * Checks if this producer is currently producing power.
     *
     * @return true if actively producing
     */
    boolean isProducing();

    /**
     * Gets the fuel level as a percentage (0.0 to 1.0).
     * For fuel-based generators. Solar panels return 1.0 during day.
     *
     * @return fuel percentage, or 1.0 if not applicable
     */
    default double getFuelLevel() {
        return 1.0;
    }

    @Override
    default PowerCapability getCapability() {
        return PowerCapability.PRODUCER;
    }
}
