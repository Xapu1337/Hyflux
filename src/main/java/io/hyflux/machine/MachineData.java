package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Container for serializing and deserializing machine state.
 * Used to persist machine data across server restarts.
 */
public class MachineData {

    private BlockPos position;
    private UUID networkId;
    private boolean active;
    private String customName;
    private final Map<String, Object> additionalData;

    public MachineData() {
        this.additionalData = new HashMap<>();
    }

    // ==================== Core Properties ====================

    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }

    @Nullable
    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(@Nullable UUID networkId) {
        this.networkId = networkId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Nullable
    public String getCustomName() {
        return customName;
    }

    public void setCustomName(@Nullable String customName) {
        this.customName = customName;
    }

    // ==================== Additional Data ====================

    /**
     * Sets an additional data value.
     */
    public void set(String key, Object value) {
        additionalData.put(key, value);
    }

    /**
     * Gets an additional data value.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(String key, Class<T> type) {
        Object value = additionalData.get(key);
        if (value == null || !type.isInstance(value)) {
            return null;
        }
        return (T) value;
    }

    /**
     * Gets an additional data value with a default.
     */
    public <T> T get(String key, Class<T> type, T defaultValue) {
        T value = get(key, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an int value.
     */
    public int getInt(String key, int defaultValue) {
        Number value = get(key, Number.class);
        return value != null ? value.intValue() : defaultValue;
    }

    /**
     * Gets a double value.
     */
    public double getDouble(String key, double defaultValue) {
        Number value = get(key, Number.class);
        return value != null ? value.doubleValue() : defaultValue;
    }

    /**
     * Gets a boolean value.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = get(key, Boolean.class);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a string value.
     */
    @Nullable
    public String getString(String key) {
        return get(key, String.class);
    }

    /**
     * Gets a string value with a default.
     */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Checks if a key exists.
     */
    public boolean has(String key) {
        return additionalData.containsKey(key);
    }

    // ==================== Compatibility Aliases ====================

    /**
     * Alias for set() for compatibility.
     */
    public void setCustomData(String key, Object value) {
        set(key, value);
    }

    /**
     * Alias for get() for compatibility.
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, T defaultValue) {
        Object value = additionalData.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Gets all additional data.
     */
    public Map<String, Object> getAdditionalData() {
        return new HashMap<>(additionalData);
    }
}
