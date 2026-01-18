package io.fabrica;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import io.fabrica.api.FabricaAPI;
import io.fabrica.command.PowerCommand;
import io.fabrica.command.GiveCommand;
import io.fabrica.machine.MachineRegistry;
import io.fabrica.power.PowerNetworkManagerImpl;
import io.fabrica.power.PowerTicker;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main plugin class for Fabrica - Factory Automation Mod.
 * Initializes the power system, machine registry, and event handlers.
 */
public class FabricaPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static FabricaPlugin instance;

    private PowerNetworkManagerImpl powerManager;
    private PowerTicker powerTicker;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> tickerFuture;

    public FabricaPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Fabrica " + this.getManifest().getVersion().toString() + " - Factory automation from steam age to quantum tech");
    }

    /**
     * Gets the plugin instance.
     */
    public static FabricaPlugin getInstance() {
        return instance;
    }

    @Override
    public CompletableFuture<Void> preLoad() {
        LOGGER.atInfo().log("Pre-loading Fabrica systems...");

        // Initialize power network manager
        powerManager = new PowerNetworkManagerImpl();
        powerTicker = new PowerTicker(powerManager);

        // Initialize the public API
        FabricaAPI.initialize(powerManager, this.getManifest().getVersion().toString());

        LOGGER.atInfo().log("Fabrica API initialized");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up Fabrica...");

        // Register commands
        registerCommands();

        // Register event handlers
        registerEventHandlers();

        LOGGER.atInfo().log("Fabrica setup complete");
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Starting Fabrica systems...");

        // Create scheduler for power tick
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Fabrica-PowerTicker");
            t.setDaemon(true);
            return t;
        });

        // Start the power network ticker (20 ticks per second = 50ms interval)
        tickerFuture = scheduler.scheduleAtFixedRate(
            powerTicker,
            0, 50, TimeUnit.MILLISECONDS
        );

        LOGGER.atInfo().log("Power system started - running at 20 ticks/second");
        LOGGER.atInfo().log("Fabrica is now active!");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down Fabrica...");

        // Cancel the ticker task
        if (tickerFuture != null) {
            tickerFuture.cancel(false);
        }

        // Shutdown scheduler
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Stop the power ticker
        if (powerTicker != null) {
            powerTicker.stop();
            LOGGER.atInfo().log(powerTicker.getStatsSummary());
        }

        // Clear machine registry
        MachineRegistry.getInstance().clear();

        // Shutdown API
        FabricaAPI.shutdown();

        LOGGER.atInfo().log("Fabrica shutdown complete");
    }

    /**
     * Registers all Fabrica commands.
     */
    private void registerCommands() {
        String pluginName = this.getName();
        String pluginVersion = this.getManifest().getVersion().toString();

        // Main /fabrica command
        this.getCommandRegistry().registerCommand(new FabricaCommand(pluginName, pluginVersion));

        // Debug commands
        this.getCommandRegistry().registerCommand(new PowerCommand(powerManager));
        this.getCommandRegistry().registerCommand(new GiveCommand());

        LOGGER.atInfo().log("Registered commands: /fabrica, /fabrica power, /fabrica give");
    }

    /**
     * Registers event handlers for block placement, breaking, and interaction.
     */
    private void registerEventHandlers() {
        // Block placement - register machine when Fabrica block is placed
        // getEventRegistry().register(PlaceBlockEvent.class, this::onBlockPlace);

        // Block breaking - unregister machine when broken
        // getEventRegistry().register(BreakBlockEvent.class, this::onBlockBreak);

        // Block interaction - open machine GUI
        // getEventRegistry().register(UseBlockEvent.Pre.class, this::onBlockInteract);

        // Note: Event handlers are commented out as they depend on the specific
        // Hytale block/item IDs we define. Uncomment and implement when testing.

        LOGGER.atInfo().log("Event handlers registered (placeholder - implement with actual block IDs)");
    }

    /**
     * Gets the power network manager.
     */
    public PowerNetworkManagerImpl getPowerManager() {
        return powerManager;
    }

    /**
     * Gets the power ticker for statistics.
     */
    public PowerTicker getPowerTicker() {
        return powerTicker;
    }
}
