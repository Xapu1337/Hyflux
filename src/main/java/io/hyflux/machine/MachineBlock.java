package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;
import io.fabrica.api.power.IPowerCapable;
import io.fabrica.api.power.PowerCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Base class for all Fabrica machines.
 * Provides common functionality for power integration and machine lifecycle.
 */
public abstract class MachineBlock implements IPowerCapable {

    protected final String machineId;
    protected final BlockPos position;
    protected UUID networkId;
    protected boolean active;
    protected String customName;

    /**
     * Creates a new machine at the given position.
     *
     * @param machineId the machine type identifier
     * @param position the block position of this machine
     */
    protected MachineBlock(@Nonnull String machineId, @Nonnull BlockPos position) {
        this.machineId = machineId;
        this.position = position;
        this.networkId = null;
        this.active = false;
        this.customName = null;
    }

    /**
     * Gets the machine type identifier.
     */
    @Nonnull
    public String getMachineId() {
        return machineId;
    }

    // ==================== IPowerCapable Implementation ====================

    @Override
    @Nonnull
    public BlockPos getPosition() {
        return position;
    }

    @Override
    @Nullable
    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(@Nullable UUID networkId) {
        this.networkId = networkId;
    }

    @Override
    public abstract PowerCapability getCapability();

    // ==================== Machine Lifecycle ====================

    /**
     * Called every game tick (20 times per second).
     * Subclasses should override to implement machine logic.
     */
    public abstract void tick();

    /**
     * Called when a player interacts with this machine.
     * Typically opens the machine's GUI.
     *
     * @param playerUuid the UUID of the interacting player
     */
    public abstract void onInteract(@Nonnull UUID playerUuid);

    /**
     * Called when this machine is placed in the world.
     */
    public void onPlace() {
        // Default: no-op, subclasses can override
    }

    /**
     * Alias for onPlace() for compatibility.
     */
    public void onPlaced() {
        onPlace();
    }

    /**
     * Called when this machine is broken/removed from the world.
     * Should handle dropping items, cleanup, etc.
     */
    public void onBreak() {
        // Default: no-op, subclasses can override
    }

    /**
     * Alias for onBreak() for compatibility.
     */
    public void onBroken() {
        onBreak();
    }

    /**
     * Called when the machine data needs to be saved.
     *
     * @return serialized machine data
     */
    @Nonnull
    public MachineData saveData() {
        MachineData data = new MachineData();
        data.set("machineId", machineId);
        data.setPosition(position);
        data.setNetworkId(networkId);
        data.setActive(active);
        data.setCustomName(customName);
        saveAdditionalData(data);
        return data;
    }

    /**
     * Serializes this machine to MachineData.
     * Alias for saveData() for consistency with ProcessingMachine.
     */
    @Nonnull
    public MachineData serialize() {
        return saveData();
    }

    /**
     * Deserializes machine state from MachineData.
     * Alias for loadData() for consistency with ProcessingMachine.
     */
    public void deserialize(@Nonnull MachineData data) {
        loadData(data);
    }

    /**
     * Called when the machine data needs to be loaded.
     *
     * @param data the serialized machine data
     */
    public void loadData(@Nonnull MachineData data) {
        this.active = data.isActive();
        this.customName = data.getCustomName();
        loadAdditionalData(data);
    }

    /**
     * Override to save additional machine-specific data.
     *
     * @param data the data container to save to
     */
    protected void saveAdditionalData(@Nonnull MachineData data) {
        // Default: no-op
    }

    /**
     * Override to load additional machine-specific data.
     *
     * @param data the data container to load from
     */
    protected void loadAdditionalData(@Nonnull MachineData data) {
        // Default: no-op
    }

    // ==================== Getters and Setters ====================

    /**
     * Checks if this machine is currently active/running.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active state of this machine.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the custom name of this machine, if set.
     */
    @Nullable
    public String getCustomName() {
        return customName;
    }

    /**
     * Sets a custom name for this machine.
     */
    public void setCustomName(@Nullable String customName) {
        this.customName = customName;
    }

    /**
     * Gets the display name of this machine.
     * Returns the custom name if set, otherwise the machine type name.
     */
    @Nonnull
    public String getDisplayName() {
        return customName != null ? customName : getMachineTypeName();
    }

    /**
     * Gets the type name of this machine (e.g., "Generator", "Macerator").
     */
    @Nonnull
    public abstract String getMachineTypeName();

    /**
     * Gets the machine type identifier used for registration.
     */
    @Nonnull
    public abstract String getMachineTypeId();

    // ==================== Utility Methods ====================

    /**
     * Checks if this machine is connected to a power network.
     */
    public boolean isConnectedToNetwork() {
        return networkId != null;
    }

    @Override
    public String toString() {
        return getMachineTypeName() + " at " + position;
    }
}
