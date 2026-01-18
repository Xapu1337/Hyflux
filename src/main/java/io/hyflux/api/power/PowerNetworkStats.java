package io.fabrica.api.power;

/**
 * Statistics for a power network tick.
 * Provides detailed information about energy flow during the last tick.
 */
public final class PowerNetworkStats {

    private final double energyProduced;      // Joules produced this tick
    private final double energyConsumed;      // Joules consumed this tick
    private final double energyCharged;       // Joules added to storage this tick
    private final double energyDischarged;    // Joules extracted from storage this tick
    private final double energyWasted;        // Joules that couldn't be used or stored
    private final double satisfactionRatio;   // 0.0 to 1.0, how much of demand was met

    public PowerNetworkStats(
            double energyProduced,
            double energyConsumed,
            double energyCharged,
            double energyDischarged,
            double energyWasted,
            double satisfactionRatio) {
        this.energyProduced = energyProduced;
        this.energyConsumed = energyConsumed;
        this.energyCharged = energyCharged;
        this.energyDischarged = energyDischarged;
        this.energyWasted = energyWasted;
        this.satisfactionRatio = satisfactionRatio;
    }

    /**
     * Gets the total energy produced by all producers this tick.
     *
     * @return energy in Joules (J)
     */
    public double getEnergyProduced() {
        return energyProduced;
    }

    /**
     * Gets the total energy consumed by all consumers this tick.
     *
     * @return energy in Joules (J)
     */
    public double getEnergyConsumed() {
        return energyConsumed;
    }

    /**
     * Gets the energy added to storage this tick.
     *
     * @return energy in Joules (J)
     */
    public double getEnergyCharged() {
        return energyCharged;
    }

    /**
     * Gets the energy extracted from storage this tick.
     *
     * @return energy in Joules (J)
     */
    public double getEnergyDischarged() {
        return energyDischarged;
    }

    /**
     * Gets the energy that was wasted (produced but couldn't be used or stored).
     *
     * @return energy in Joules (J)
     */
    public double getEnergyWasted() {
        return energyWasted;
    }

    /**
     * Gets the ratio of demand that was satisfied (0.0 to 1.0).
     * 1.0 means all consumers received full power.
     *
     * @return satisfaction ratio
     */
    public double getSatisfactionRatio() {
        return satisfactionRatio;
    }

    /**
     * Checks if the network had a power deficit this tick.
     *
     * @return true if demand exceeded supply + storage
     */
    public boolean hadDeficit() {
        return satisfactionRatio < 1.0;
    }

    /**
     * Checks if the network had surplus power this tick.
     *
     * @return true if production exceeded consumption
     */
    public boolean hadSurplus() {
        return energyProduced > energyConsumed;
    }

    @Override
    public String toString() {
        return String.format(
            "NetworkStats[produced=%.2fJ, consumed=%.2fJ, charged=%.2fJ, discharged=%.2fJ, wasted=%.2fJ, satisfaction=%.1f%%]",
            energyProduced, energyConsumed, energyCharged, energyDischarged, energyWasted, satisfactionRatio * 100
        );
    }
}
