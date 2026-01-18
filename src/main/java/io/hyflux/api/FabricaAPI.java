package io.fabrica.api;

import io.fabrica.api.power.IPowerNetworkManager;

/**
 * Main entry point for the Fabrica API.
 * Other mods can use this to access Fabrica's systems.
 *
 * <p>Usage example:
 * <pre>{@code
 * if (FabricaAPI.isAvailable()) {
 *     IPowerNetworkManager powerManager = FabricaAPI.getPowerManager();
 *     // Use power API...
 * }
 * }</pre>
 */
public final class FabricaAPI {

    private static IPowerNetworkManager powerManager;
    private static String version;
    private static boolean initialized;

    private FabricaAPI() {
        // Static utility class
    }

    /**
     * Checks if Fabrica is available and initialized.
     *
     * @return true if Fabrica is loaded and ready
     */
    public static boolean isAvailable() {
        return initialized && powerManager != null;
    }

    /**
     * Gets the power network manager.
     *
     * @return the power network manager, or null if not available
     */
    public static IPowerNetworkManager getPowerManager() {
        return powerManager;
    }

    /**
     * Gets the Fabrica version.
     *
     * @return the version string, or "unknown" if not available
     */
    public static String getVersion() {
        return version != null ? version : "unknown";
    }

    /**
     * Checks if the power system is enabled.
     *
     * @return true if the power system is available
     */
    public static boolean isPowerSystemEnabled() {
        return powerManager != null;
    }

    // ==================== Internal Methods ====================
    // These are called by FabricaPlugin during initialization

    /**
     * Initializes the Fabrica API.
     * Called by FabricaPlugin on setup.
     *
     * @param manager the power network manager instance
     * @param ver the plugin version
     */
    public static void initialize(IPowerNetworkManager manager, String ver) {
        powerManager = manager;
        version = ver;
        initialized = true;
    }

    /**
     * Shuts down the Fabrica API.
     * Called by FabricaPlugin on shutdown.
     */
    public static void shutdown() {
        powerManager = null;
        initialized = false;
    }

    /**
     * Checks if the API is initialized.
     *
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
