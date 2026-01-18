package io.fabrica.transport;

import io.fabrica.machine.ItemStack;

/**
 * Represents an item on a conveyor belt.
 * Tracks position along the belt for smooth movement.
 */
public class ConveyorItem {

    private final ItemStack itemStack;
    private double progress;  // 0.0 = start of belt, 1.0 = end of belt

    public ConveyorItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.progress = 0.0;
    }

    public ConveyorItem(ItemStack itemStack, double progress) {
        this.itemStack = itemStack;
        this.progress = progress;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    /**
     * Advances the item along the belt.
     *
     * @param amount the amount to advance (0.0 to 1.0)
     * @return true if the item reached the end of the belt
     */
    public boolean advance(double amount) {
        progress += amount;
        if (progress >= 1.0) {
            progress = 1.0;
            return true;
        }
        return false;
    }

    /**
     * Checks if this item has reached the end of the belt.
     */
    public boolean atEnd() {
        return progress >= 1.0;
    }

    @Override
    public String toString() {
        return String.format("ConveyorItem[%s @ %.2f]", itemStack, progress);
    }
}
