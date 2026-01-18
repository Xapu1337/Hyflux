package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;

import java.util.UUID;

/**
 * Macerator machine that grinds ore into dust (ore doubling).
 *
 * Stats:
 * - Power consumption: 160 W (8 J/tick)
 * - Energy per operation: 1,600 J
 * - Processing time: 10 seconds at full power
 */
public class MaceratorMachine extends ProcessingMachine {

    // Machine specs
    public static final double POWER_CONSUMPTION = 160.0;  // Watts
    public static final double ENERGY_PER_OPERATION = 1600.0;  // Joules
    public static final int PROCESSING_TICKS = 200;  // 10 seconds

    public MaceratorMachine(BlockPos position) {
        super("macerator", position, "macerator", POWER_CONSUMPTION);
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Override to open Macerator GUI
    }

    @Override
    public String getMachineTypeName() {
        return "Macerator";
    }

    @Override
    public String getMachineTypeId() {
        return "macerator";
    }

    /**
     * Gets the grinding progress as a percentage for UI display.
     */
    public double getGrindingProgress() {
        return getProgress();
    }

    @Override
    public String toString() {
        String recipeInfo = currentRecipe != null ?
            String.format("%s → %s", currentRecipe.getInputItemId(), currentRecipe.getOutputItemId()) :
            "idle";
        return String.format("Macerator[pos=%s, progress=%.1f%%, recipe=%s]",
            position, getProgress() * 100, recipeInfo);
    }
}
