package io.fabrica.machine;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a processing recipe for machines like Macerator and Electric Furnace.
 * Defines input item, output item, and energy cost.
 */
public class Recipe {

    private final String id;
    private final String inputItemId;
    private final int inputCount;
    private final String outputItemId;
    private final int outputCount;
    private final double energyRequired;  // Joules
    private final String machineType;     // Which machine can process this

    /**
     * Creates a new recipe.
     *
     * @param id unique recipe identifier
     * @param inputItemId the input item type ID
     * @param inputCount number of input items consumed
     * @param outputItemId the output item type ID
     * @param outputCount number of output items produced
     * @param energyRequired energy required in Joules
     * @param machineType the machine type that can process this recipe
     */
    public Recipe(
            @Nonnull String id,
            @Nonnull String inputItemId,
            int inputCount,
            @Nonnull String outputItemId,
            int outputCount,
            double energyRequired,
            @Nonnull String machineType) {
        this.id = Objects.requireNonNull(id, "Recipe id cannot be null");
        this.inputItemId = Objects.requireNonNull(inputItemId, "Input item id cannot be null");
        this.inputCount = inputCount;
        this.outputItemId = Objects.requireNonNull(outputItemId, "Output item id cannot be null");
        this.outputCount = outputCount;
        this.energyRequired = energyRequired;
        this.machineType = Objects.requireNonNull(machineType, "Machine type cannot be null");
    }

    /**
     * Gets the unique recipe identifier.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * Alias for getId() for compatibility.
     */
    @Nonnull
    public String getRecipeId() {
        return id;
    }

    /**
     * Gets the input item type ID.
     */
    @Nonnull
    public String getInputItemId() {
        return inputItemId;
    }

    /**
     * Alias for getInputItemId() for compatibility.
     */
    @Nonnull
    public String getInputItem() {
        return inputItemId;
    }

    /**
     * Gets the number of input items consumed.
     */
    public int getInputCount() {
        return inputCount;
    }

    /**
     * Gets the output item type ID.
     */
    @Nonnull
    public String getOutputItemId() {
        return outputItemId;
    }

    /**
     * Alias for getOutputItemId() for compatibility.
     */
    @Nonnull
    public String getOutputItem() {
        return outputItemId;
    }

    /**
     * Gets the number of output items produced.
     */
    public int getOutputCount() {
        return outputCount;
    }

    /**
     * Gets the energy required to complete this recipe in Joules.
     */
    public double getEnergyRequired() {
        return energyRequired;
    }

    /**
     * Gets the processing time in seconds at the given power consumption rate.
     *
     * @param powerWatts the power consumption in Watts
     * @return processing time in seconds
     */
    public double getProcessingTime(double powerWatts) {
        if (powerWatts <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return energyRequired / powerWatts;
    }

    /**
     * Gets the processing time in ticks at the given power consumption rate.
     *
     * @param powerWatts the power consumption in Watts
     * @return processing time in ticks (20 ticks = 1 second)
     */
    public int getProcessingTicks(double powerWatts) {
        return (int) Math.ceil(getProcessingTime(powerWatts) * 20);
    }

    /**
     * Gets the machine type that can process this recipe.
     */
    @Nonnull
    public String getMachineType() {
        return machineType;
    }

    /**
     * Checks if a given item matches this recipe's input.
     *
     * @param itemId the item type ID to check
     * @return true if this item can be used as input
     */
    public boolean matchesInput(@Nonnull String itemId) {
        return inputItemId.equals(itemId);
    }

    /**
     * Checks if a given item matches this recipe's input with sufficient quantity.
     *
     * @param itemId the item type ID to check
     * @param count the available quantity
     * @return true if this item can be used as input with sufficient quantity
     */
    public boolean matchesInput(@Nonnull String itemId, int count) {
        return inputItemId.equals(itemId) && count >= inputCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Recipe{" + inputCount + "x " + inputItemId +
                " -> " + outputCount + "x " + outputItemId +
                " (" + energyRequired + " J)}";
    }

    // ==================== Builder ====================

    /**
     * Creates a builder for constructing recipes.
     */
    public static Builder builder(@Nonnull String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String inputItemId;
        private int inputCount = 1;
        private String outputItemId;
        private int outputCount = 1;
        private double energyRequired;
        private String machineType;

        private Builder(String id) {
            this.id = id;
        }

        public Builder input(@Nonnull String itemId) {
            this.inputItemId = itemId;
            return this;
        }

        public Builder input(@Nonnull String itemId, int count) {
            this.inputItemId = itemId;
            this.inputCount = count;
            return this;
        }

        public Builder output(@Nonnull String itemId) {
            this.outputItemId = itemId;
            return this;
        }

        public Builder output(@Nonnull String itemId, int count) {
            this.outputItemId = itemId;
            this.outputCount = count;
            return this;
        }

        public Builder energy(double joules) {
            this.energyRequired = joules;
            return this;
        }

        public Builder machine(@Nonnull String machineType) {
            this.machineType = machineType;
            return this;
        }

        public Recipe build() {
            return new Recipe(id, inputItemId, inputCount, outputItemId, outputCount, energyRequired, machineType);
        }
    }
}
