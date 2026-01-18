package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.IPowerNetworkManager;
import io.fabrica.api.power.IPowerProducer;
import io.fabrica.api.power.PowerCapability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Solid fuel generator that burns fuel to produce power.
 *
 * Stats:
 * - Power output: 400 W (20 J/tick)
 * - Internal buffer: 1,000 J
 * - Fuel slot: 1 stack
 */
public class GeneratorMachine extends MachineBlock implements IPowerProducer {

    // Power specs
    public static final double POWER_OUTPUT = 400.0;  // Watts
    public static final double INTERNAL_BUFFER = 1000.0;  // Joules

    // Fuel energy values (Joules)
    private static final Map<String, Double> FUEL_VALUES = new HashMap<>();
    static {
        FUEL_VALUES.put("Coal", 32000.0);
        FUEL_VALUES.put("Charcoal", 16000.0);
        FUEL_VALUES.put("Wood", 6000.0);
        FUEL_VALUES.put("Oak_Log", 6000.0);
        FUEL_VALUES.put("Birch_Log", 6000.0);
        FUEL_VALUES.put("Spruce_Log", 6000.0);
        FUEL_VALUES.put("Planks", 3000.0);
    }

    private ItemStack fuelSlot;
    private double storedEnergy;      // Joules in internal buffer
    private double currentBurnEnergy; // Joules remaining in current fuel item
    private double totalBurnEnergy;   // Total Joules for current fuel item (for progress)
    private String currentFuelType;

    public GeneratorMachine(BlockPos position) {
        super("generator", position);
        this.fuelSlot = ItemStack.empty();
        this.storedEnergy = 0;
        this.currentBurnEnergy = 0;
        this.totalBurnEnergy = 0;
        this.currentFuelType = "";
    }

    @Override
    public PowerCapability getCapability() {
        return PowerCapability.PRODUCER;
    }

    @Override
    public double getMaxProductionRate() {
        return POWER_OUTPUT;
    }

    @Override
    public double getCurrentProductionRate() {
        return isProducing() ? POWER_OUTPUT : 0;
    }

    @Override
    public double produce(double maxJoules) {
        if (!isProducing()) {
            return 0;
        }

        // Transfer from buffer to network
        double toTransfer = Math.min(maxJoules, storedEnergy);
        storedEnergy -= toTransfer;
        return toTransfer;
    }

    @Override
    public boolean isProducing() {
        // Producing if we have energy in buffer OR we're burning fuel
        return storedEnergy > 0 || currentBurnEnergy > 0 || canStartBurning();
    }

    @Override
    public double getFuelLevel() {
        if (totalBurnEnergy <= 0) {
            return fuelSlot.isEmpty() ? 0 : 1.0;
        }
        return currentBurnEnergy / totalBurnEnergy;
    }

    @Override
    public void tick() {
        // Burn fuel to fill internal buffer
        if (storedEnergy < INTERNAL_BUFFER) {
            double energyNeeded = INTERNAL_BUFFER - storedEnergy;

            // Try to use current burning fuel
            if (currentBurnEnergy > 0) {
                double toBurn = Math.min(energyNeeded, IPowerNetworkManager.wattsToJoulesPerTick(POWER_OUTPUT));
                toBurn = Math.min(toBurn, currentBurnEnergy);
                storedEnergy += toBurn;
                currentBurnEnergy -= toBurn;
            }

            // Try to start burning new fuel if needed
            if (currentBurnEnergy <= 0 && storedEnergy < INTERNAL_BUFFER) {
                tryStartBurning();
            }
        }

        // Update active state
        setActive(isProducing());
    }

    /**
     * Attempts to start burning a new piece of fuel.
     */
    private void tryStartBurning() {
        if (fuelSlot.isEmpty()) {
            currentFuelType = "";
            return;
        }

        Double fuelValue = FUEL_VALUES.get(fuelSlot.getItemId());
        if (fuelValue != null && fuelValue > 0) {
            fuelSlot.remove(1);
            currentBurnEnergy = fuelValue;
            totalBurnEnergy = fuelValue;
            currentFuelType = fuelSlot.getItemId();
        }
    }

    /**
     * Checks if we can start burning new fuel.
     */
    private boolean canStartBurning() {
        if (fuelSlot.isEmpty()) {
            return false;
        }
        return FUEL_VALUES.containsKey(fuelSlot.getItemId());
    }

    /**
     * Gets the stored energy in the internal buffer.
     */
    public double getStoredEnergy() {
        return storedEnergy;
    }

    /**
     * Gets the fuel slot.
     */
    public ItemStack getFuelSlot() {
        return fuelSlot;
    }

    /**
     * Inserts fuel into the generator.
     */
    public ItemStack insertFuel(ItemStack stack) {
        if (!FUEL_VALUES.containsKey(stack.getItemId())) {
            return stack; // Not valid fuel
        }
        return fuelSlot.add(stack);
    }

    /**
     * Gets the burn progress (0.0 to 1.0).
     */
    public double getBurnProgress() {
        if (totalBurnEnergy <= 0) {
            return 0;
        }
        return 1.0 - (currentBurnEnergy / totalBurnEnergy);
    }

    /**
     * Gets the current fuel type being burned.
     */
    public String getCurrentFuelType() {
        return currentFuelType;
    }

    /**
     * Checks if an item is valid fuel.
     */
    public static boolean isValidFuel(String itemId) {
        return FUEL_VALUES.containsKey(itemId);
    }

    /**
     * Gets the energy value of a fuel item.
     */
    public static double getFuelValue(String itemId) {
        return FUEL_VALUES.getOrDefault(itemId, 0.0);
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Override to open GUI
    }

    @Override
    protected void saveAdditionalData(MachineData data) {
        data.set("fuelItem", fuelSlot.getItemId());
        data.set("fuelCount", fuelSlot.getCount());
        data.set("storedEnergy", storedEnergy);
        data.set("currentBurnEnergy", currentBurnEnergy);
        data.set("totalBurnEnergy", totalBurnEnergy);
        data.set("currentFuelType", currentFuelType);
    }

    @Override
    protected void loadAdditionalData(MachineData data) {
        String fuelItem = data.getString("fuelItem");
        int fuelCount = data.getInt("fuelCount", 0);
        fuelSlot = ItemStack.of(fuelItem != null ? fuelItem : "", fuelCount);
        storedEnergy = data.getDouble("storedEnergy", 0);
        currentBurnEnergy = data.getDouble("currentBurnEnergy", 0);
        totalBurnEnergy = data.getDouble("totalBurnEnergy", 0);
        String fuelType = data.getString("currentFuelType");
        currentFuelType = fuelType != null ? fuelType : "";
    }

    @Override
    public String getMachineTypeName() {
        return "Generator";
    }

    @Override
    public String getMachineTypeId() {
        return "generator";
    }
}
