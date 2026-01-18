package io.fabrica.machine;

import java.util.Objects;

/**
 * Represents a stack of items.
 * This is a simple data class for managing item counts.
 */
public class ItemStack {

    public static final ItemStack EMPTY = new ItemStack("", 0);
    public static final int MAX_STACK_SIZE = 64;

    private String itemId;
    private int count;

    public ItemStack(String itemId, int count) {
        this.itemId = itemId;
        this.count = Math.max(0, count);
    }

    public ItemStack(String itemId) {
        this(itemId, 1);
    }

    public static ItemStack empty() {
        return EMPTY;
    }

    public static ItemStack of(String itemId, int count) {
        if (itemId == null || itemId.isEmpty() || count <= 0) {
            return EMPTY;
        }
        return new ItemStack(itemId, count);
    }

    public String getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return count <= 0 || itemId == null || itemId.isEmpty();
    }

    /**
     * Checks if this stack can merge with another stack.
     */
    public boolean canMergeWith(ItemStack other) {
        if (this.isEmpty()) return true;
        if (other.isEmpty()) return true;
        return this.itemId.equals(other.itemId) && this.count + other.count <= MAX_STACK_SIZE;
    }

    /**
     * Attempts to add items to this stack.
     *
     * @param toAdd the stack to add
     * @return the overflow (items that couldn't fit), or EMPTY if all fit
     */
    public ItemStack add(ItemStack toAdd) {
        if (toAdd.isEmpty()) {
            return EMPTY;
        }

        if (this.isEmpty()) {
            this.itemId = toAdd.itemId;
            this.count = Math.min(toAdd.count, MAX_STACK_SIZE);
            int overflow = toAdd.count - this.count;
            return overflow > 0 ? new ItemStack(toAdd.itemId, overflow) : EMPTY;
        }

        if (!this.itemId.equals(toAdd.itemId)) {
            return toAdd; // Can't merge different items
        }

        int spaceAvailable = MAX_STACK_SIZE - this.count;
        int toTransfer = Math.min(spaceAvailable, toAdd.count);
        this.count += toTransfer;

        int overflow = toAdd.count - toTransfer;
        return overflow > 0 ? new ItemStack(toAdd.itemId, overflow) : EMPTY;
    }

    /**
     * Removes items from this stack.
     *
     * @param amount the number of items to remove
     * @return the removed stack
     */
    public ItemStack remove(int amount) {
        if (this.isEmpty() || amount <= 0) {
            return EMPTY;
        }

        int toRemove = Math.min(amount, this.count);
        String removedId = this.itemId;

        this.count -= toRemove;
        if (this.count <= 0) {
            this.itemId = "";
            this.count = 0;
        }

        return new ItemStack(removedId, toRemove);
    }

    /**
     * Creates a copy of this stack.
     */
    public ItemStack copy() {
        return new ItemStack(itemId, count);
    }

    /**
     * Clears this stack.
     */
    public void clear() {
        this.itemId = "";
        this.count = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStack itemStack = (ItemStack) o;
        return count == itemStack.count && Objects.equals(itemId, itemStack.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, count);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "ItemStack[EMPTY]";
        }
        return String.format("ItemStack[%s x%d]", itemId, count);
    }
}
