package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.IPowerConsumer;
import io.fabrica.api.power.IPowerNetworkManager;
import io.fabrica.api.power.PowerCapability;

import java.util.UUID;

/**
 * Base class for machines that process items using recipes.
 * Handles input/output slots, recipe matching, and energy consumption.
 */
public abstract class ProcessingMachine extends MachineBlock implements IPowerConsumer {

    protected ItemStack inputSlot;
    protected ItemStack outputSlot;
    protected Recipe currentRecipe;
    protected double energyBuffer;      // Joules accumulated for current operation
    protected int progressTicks;        // Progress in ticks
    protected final String recipeType;  // Machine type for recipe lookup
    protected final double powerConsumption;  // Watts

    public ProcessingMachine(String machineId, BlockPos position, String recipeType, double powerConsumption) {
        super(machineId, position);
        this.inputSlot = ItemStack.empty();
        this.outputSlot = ItemStack.empty();
        this.currentRecipe = null;
        this.energyBuffer = 0;
        this.progressTicks = 0;
        this.recipeType = recipeType;
        this.powerConsumption = powerConsumption;
    }

    @Override
    public PowerCapability getCapability() {
        return PowerCapability.CONSUMER;
    }

    @Override
    public double getConsumptionRate() {
        return powerConsumption;
    }

    @Override
    public boolean canOperate() {
        // Can operate if we have a valid recipe and space for output
        if (currentRecipe == null) {
            findAndSetRecipe();
        }
        return currentRecipe != null && canOutputResult();
    }

    @Override
    public double consume(double availableJoules) {
        if (!canOperate()) {
            return 0;
        }

        // Calculate how much energy we need this tick
        double neededJoules = IPowerNetworkManager.wattsToJoulesPerTick(powerConsumption);
        double consumed = Math.min(availableJoules, neededJoules);

        energyBuffer += consumed;
        progressTicks++;

        // Check if recipe is complete
        if (energyBuffer >= currentRecipe.getEnergyRequired()) {
            completeRecipe();
        }

        return consumed;
    }

    @Override
    public double getEnergyBuffer() {
        return energyBuffer;
    }

    @Override
    public double getMaxEnergyBuffer() {
        return currentRecipe != null ? currentRecipe.getEnergyRequired() : 0;
    }

    @Override
    public void tick() {
        // Update active state
        setActive(canOperate() && energyBuffer > 0);

        // If no power this tick, don't increment progress
        // The consume() method handles progress when power is available
    }

    /**
     * Finds a recipe matching the current input and sets it.
     */
    protected void findAndSetRecipe() {
        if (inputSlot.isEmpty()) {
            currentRecipe = null;
            energyBuffer = 0;
            progressTicks = 0;
            return;
        }

        Recipe recipe = RecipeRegistry.getInstance().findRecipe(recipeType, inputSlot.getItemId());
        if (recipe != null && inputSlot.getCount() >= recipe.getInputCount()) {
            // Check if output can accept the result
            if (canAcceptOutput(recipe.getOutputItemId(), recipe.getOutputCount())) {
                currentRecipe = recipe;
            } else {
                currentRecipe = null;
            }
        } else {
            currentRecipe = null;
        }

        // Reset progress if recipe changed
        if (currentRecipe == null) {
            energyBuffer = 0;
            progressTicks = 0;
        }
    }

    /**
     * Checks if the output slot can accept the recipe result.
     */
    protected boolean canAcceptOutput(String itemId, int count) {
        if (outputSlot.isEmpty()) {
            return true;
        }
        return outputSlot.getItemId().equals(itemId) &&
               outputSlot.getCount() + count <= ItemStack.MAX_STACK_SIZE;
    }

    /**
     * Checks if the current recipe result can be output.
     */
    protected boolean canOutputResult() {
        if (currentRecipe == null) {
            return false;
        }
        return canAcceptOutput(currentRecipe.getOutputItemId(), currentRecipe.getOutputCount());
    }

    /**
     * Completes the current recipe, consuming input and producing output.
     */
    protected void completeRecipe() {
        if (currentRecipe == null) {
            return;
        }

        // Consume input
        inputSlot.remove(currentRecipe.getInputCount());

        // Produce output
        ItemStack result = ItemStack.of(currentRecipe.getOutputItemId(), currentRecipe.getOutputCount());
        outputSlot.add(result);

        // Reset for next operation
        energyBuffer = 0;
        progressTicks = 0;
        currentRecipe = null;

        // Try to start next operation
        findAndSetRecipe();
    }

    /**
     * Gets the current processing progress as a percentage (0.0 to 1.0).
     */
    public double getProgress() {
        if (currentRecipe == null || currentRecipe.getEnergyRequired() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, energyBuffer / currentRecipe.getEnergyRequired());
    }

    /**
     * Gets the progress in ticks.
     */
    public int getProgressTicks() {
        return progressTicks;
    }

    /**
     * Gets the input slot.
     */
    public ItemStack getInputSlot() {
        return inputSlot;
    }

    /**
     * Gets the output slot.
     */
    public ItemStack getOutputSlot() {
        return outputSlot;
    }

    /**
     * Attempts to insert an item into the input slot.
     *
     * @param stack the item stack to insert
     * @return the overflow (items that couldn't fit)
     */
    public ItemStack insertInput(ItemStack stack) {
        ItemStack overflow = inputSlot.add(stack);
        findAndSetRecipe();
        return overflow;
    }

    /**
     * Extracts items from the output slot.
     *
     * @param maxAmount maximum items to extract
     * @return the extracted items
     */
    public ItemStack extractOutput(int maxAmount) {
        return outputSlot.remove(maxAmount);
    }

    /**
     * Gets the current recipe being processed.
     */
    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public void onInteract(UUID playerUuid) {
        // Override in subclass to open GUI
    }

    @Override
    protected void saveAdditionalData(MachineData data) {
        data.set("inputItem", inputSlot.getItemId());
        data.set("inputCount", inputSlot.getCount());
        data.set("outputItem", outputSlot.getItemId());
        data.set("outputCount", outputSlot.getCount());
        data.set("energyBuffer", energyBuffer);
        data.set("progressTicks", progressTicks);
    }

    @Override
    protected void loadAdditionalData(MachineData data) {
        String inputItem = data.getString("inputItem");
        int inputCount = data.getInt("inputCount", 0);
        inputSlot = ItemStack.of(inputItem != null ? inputItem : "", inputCount);

        String outputItem = data.getString("outputItem");
        int outputCount = data.getInt("outputCount", 0);
        outputSlot = ItemStack.of(outputItem != null ? outputItem : "", outputCount);

        energyBuffer = data.getDouble("energyBuffer", 0);
        progressTicks = data.getInt("progressTicks", 0);

        findAndSetRecipe();
    }
}
