package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.IPowerStorage;
import io.fabrica.api.power.PowerCapability;

import java.util.UUID;

/**
 * Battery block that stores power for later use.
 *
 * Stats:
 * - Capacity: 100,000 J (100 kJ)
 * - Max charge rate: 640 W (32 J/tick)
 * - Max discharge rate: 640 W (32 J/tick)
 */
public class BatteryMachine extends MachineBlock implements IPowerStorage {

    // Battery specs
    public static final double MAX_CAPACITY = 100_000.0;      // Joules
    public static final double MAX_CHARGE_RATE = 640.0;       // Watts
    public static final double MAX_DISCHARGE_RATE = 640.0;    // Watts

    private double storedEnergy;  // Joules

    public BatteryMachine(BlockPos position) {
        super("battery", position);
        this.storedEnergy = 0;
    }

    @Override
    public PowerCapability getCapability() {
        return PowerCapability.STORAGE;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public double getMaxCapacity() {
        return MAX_CAPACITY;
    }

    @Override
    public double getMaxChargeRate() {
        return MAX_CHARGE_RATE;
    }

    @Override
    public double getMaxDischargeRate() {
        return MAX_DISCHARGE_RATE;
    }

    @Override
    public double charge(double joules) {
        if (joules <= 0) {
            return 0;
        }

        double availableSpace = MAX_CAPACITY - storedEnergy;
        double toCharge = Math.min(joules, availableSpace);
        storedEnergy += toCharge;

        // Return overflow
        return joules - toCharge;
    }

    @Override
    public double discharge(double joules) {
        if (joules <= 0) {
            return 0;
        }

        double toDischarge = Math.min(joules, storedEnergy);
        storedEnergy -= toDischarge;

        return toDischarge;
    }

    @Override
    public void tick() {
        // Update active state (active if storing or providing energy)
        setActive(storedEnergy > 0);
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Override to open GUI showing charge level
    }

    /**
     * Sets the stored energy directly (for testing/debugging).
     */
    public void setStoredEnergy(double joules) {
        this.storedEnergy = Math.max(0, Math.min(joules, MAX_CAPACITY));
    }

    @Override
    protected void saveAdditionalData(MachineData data) {
        data.set("storedEnergy", storedEnergy);
    }

    @Override
    protected void loadAdditionalData(MachineData data) {
        storedEnergy = data.getDouble("storedEnergy", 0);
    }

    @Override
    public String getMachineTypeName() {
        return "Battery";
    }

    @Override
    public String getMachineTypeId() {
        return "battery";
    }

    @Override
    public String toString() {
        return String.format("Battery[pos=%s, stored=%.0fJ/%.0fJ (%.1f%%)]",
            position, storedEnergy, MAX_CAPACITY, getChargePercentage() * 100);
    }
}
