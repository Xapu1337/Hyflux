package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;

import java.util.UUID;

/**
 * Electric furnace that smelts dust/ore into ingots using electricity.
 *
 * Stats:
 * - Power consumption: 160 W (8 J/tick)
 * - Energy per operation: 1,280 J
 * - Processing time: 8 seconds at full power
 */
public class ElectricFurnaceMachine extends ProcessingMachine {

    // Machine specs
    public static final double POWER_CONSUMPTION = 160.0;  // Watts
    public static final double ENERGY_PER_OPERATION = 1280.0;  // Joules
    public static final int PROCESSING_TICKS = 160;  // 8 seconds

    public ElectricFurnaceMachine(BlockPos position) {
        super("electric_furnace", position, "electric_furnace", POWER_CONSUMPTION);
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Override to open Electric Furnace GUI
    }

    @Override
    public String getMachineTypeName() {
        return "Electric Furnace";
    }

    @Override
    public String getMachineTypeId() {
        return "electric_furnace";
    }

    /**
     * Gets the smelting progress as a percentage for UI display.
     */
    public double getSmeltingProgress() {
        return getProgress();
    }

    @Override
    public String toString() {
        String recipeInfo = currentRecipe != null ?
            String.format("%s → %s", currentRecipe.getInputItemId(), currentRecipe.getOutputItemId()) :
            "idle";
        return String.format("ElectricFurnace[pos=%s, progress=%.1f%%, recipe=%s]",
            position, getProgress() * 100, recipeInfo);
    }
}
