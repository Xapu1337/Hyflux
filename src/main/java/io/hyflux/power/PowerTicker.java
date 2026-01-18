package io.fabrica.power;

import com.hypixel.hytale.logger.HytaleLogger;

/**
 * Handles ticking of the power network system.
 * This is called periodically to update all power networks.
 */
public class PowerTicker implements Runnable {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final PowerNetworkManagerImpl networkManager;
    private volatile boolean running;
    private long tickCount;
    private long totalTickTimeNanos;
    private long maxTickTimeNanos;

    public PowerTicker(PowerNetworkManagerImpl networkManager) {
        this.networkManager = networkManager;
        this.running = true;
        this.tickCount = 0;
        this.totalTickTimeNanos = 0;
        this.maxTickTimeNanos = 0;
    }

    @Override
    public void run() {
        if (!running) {
            return;
        }

        long startTime = System.nanoTime();

        try {
            networkManager.tickAll();
            tickCount++;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Error during power network tick");
        }

        long elapsed = System.nanoTime() - startTime;
        totalTickTimeNanos += elapsed;
        maxTickTimeNanos = Math.max(maxTickTimeNanos, elapsed);
    }

    /**
     * Stops the ticker.
     */
    public void stop() {
        running = false;
    }

    /**
     * Checks if the ticker is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gets the total number of ticks processed.
     */
    public long getTickCount() {
        return tickCount;
    }

    /**
     * Gets the average tick time in milliseconds.
     */
    public double getAverageTickTimeMs() {
        return tickCount > 0 ? (totalTickTimeNanos / (double) tickCount) / 1_000_000.0 : 0.0;
    }

    /**
     * Gets the maximum tick time in milliseconds.
     */
    public double getMaxTickTimeMs() {
        return maxTickTimeNanos / 1_000_000.0;
    }

    /**
     * Resets the performance statistics.
     */
    public void resetStats() {
        tickCount = 0;
        totalTickTimeNanos = 0;
        maxTickTimeNanos = 0;
    }

    /**
     * Gets a summary of performance statistics.
     */
    public String getStatsSummary() {
        return String.format(
            "PowerTicker Stats: %d ticks, avg=%.3fms, max=%.3fms",
            tickCount, getAverageTickTimeMs(), getMaxTickTimeMs()
        );
    }
}
