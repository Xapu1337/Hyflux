package io.fabrica.power;

import io.fabrica.api.power.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of the power network manager.
 * Manages all power networks and handles block registration/unregistration.
 */
public class PowerNetworkManagerImpl implements IPowerNetworkManager {

    private final Map<UUID, PowerNetwork> networks;
    private final Map<BlockPos, IPowerCapable> allCapables;
    private final List<PowerNetworkListener> listeners;

    public PowerNetworkManagerImpl() {
        this.networks = new ConcurrentHashMap<>();
        this.allCapables = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public PowerNetworkSnapshot getNetwork(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        return network != null ? network.createSnapshot() : null;
    }

    @Override
    public PowerNetworkSnapshot getNetworkAt(BlockPos pos) {
        IPowerCapable capable = allCapables.get(pos);
        if (capable == null || capable.getNetworkId() == null) {
            return null;
        }
        return getNetwork(capable.getNetworkId());
    }

    @Override
    public Collection<PowerNetworkSnapshot> getAllNetworks() {
        List<PowerNetworkSnapshot> snapshots = new ArrayList<>();
        for (PowerNetwork network : networks.values()) {
            snapshots.add(network.createSnapshot());
        }
        return snapshots;
    }

    @Override
    public void registerCapable(IPowerCapable capable) {
        BlockPos pos = capable.getPosition();

        // Already registered?
        if (allCapables.containsKey(pos)) {
            return;
        }

        allCapables.put(pos, capable);

        // Find adjacent networks
        Set<UUID> adjacentNetworkIds = new HashSet<>();
        for (BlockPos adjacent : pos.getAdjacent()) {
            IPowerCapable adjacentCapable = allCapables.get(adjacent);
            if (adjacentCapable != null && adjacentCapable.getNetworkId() != null) {
                adjacentNetworkIds.add(adjacentCapable.getNetworkId());
            }
        }

        if (adjacentNetworkIds.isEmpty()) {
            // Create new network
            PowerNetwork network = new PowerNetwork();
            network.addMember(capable);
            networks.put(network.getNetworkId(), network);

            // Notify listeners
            for (PowerNetworkListener listener : listeners) {
                listener.onNetworkCreated(network.createSnapshot());
                listener.onCapableAdded(capable, network.getNetworkId());
            }
        } else if (adjacentNetworkIds.size() == 1) {
            // Join existing network
            UUID networkId = adjacentNetworkIds.iterator().next();
            PowerNetwork network = networks.get(networkId);
            if (network != null) {
                network.addMember(capable);

                // Notify listeners
                for (PowerNetworkListener listener : listeners) {
                    listener.onCapableAdded(capable, networkId);
                }
            }
        } else {
            // Merge multiple networks
            Iterator<UUID> iter = adjacentNetworkIds.iterator();
            UUID targetId = iter.next();
            PowerNetwork targetNetwork = networks.get(targetId);

            if (targetNetwork != null) {
                // Add the new block first
                targetNetwork.addMember(capable);

                // Merge other networks into target
                while (iter.hasNext()) {
                    UUID sourceId = iter.next();
                    PowerNetwork sourceNetwork = networks.remove(sourceId);
                    if (sourceNetwork != null) {
                        targetNetwork.merge(sourceNetwork);

                        // Notify listeners
                        for (PowerNetworkListener listener : listeners) {
                            listener.onNetworkMerged(sourceId, targetId);
                        }
                    }
                }

                // Notify listeners
                for (PowerNetworkListener listener : listeners) {
                    listener.onCapableAdded(capable, targetId);
                }
            }
        }
    }

    @Override
    public void unregisterCapable(IPowerCapable capable) {
        BlockPos pos = capable.getPosition();
        UUID networkId = capable.getNetworkId();

        if (!allCapables.containsKey(pos)) {
            return;
        }

        allCapables.remove(pos);

        if (networkId == null) {
            return;
        }

        PowerNetwork network = networks.get(networkId);
        if (network == null) {
            return;
        }

        // Remove from network
        network.removeMember(capable);

        // Notify listeners
        for (PowerNetworkListener listener : listeners) {
            listener.onCapableRemoved(capable, networkId);
        }

        // Check if network needs to be split or removed
        if (network.isEmpty()) {
            networks.remove(networkId);
        } else if (!network.isConnected()) {
            // Network split - remove old network and create new ones
            networks.remove(networkId);

            List<PowerNetwork> fragments = network.splitIntoComponents();
            List<UUID> fragmentIds = new ArrayList<>();

            for (PowerNetwork fragment : fragments) {
                networks.put(fragment.getNetworkId(), fragment);
                fragmentIds.add(fragment.getNetworkId());
            }

            // Notify listeners
            for (PowerNetworkListener listener : listeners) {
                listener.onNetworkSplit(networkId, fragmentIds);
            }
        }
    }

    @Override
    public IPowerCapable getCapableAt(BlockPos pos) {
        return allCapables.get(pos);
    }

    @Override
    public double getNetworkProduction(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        if (network == null) {
            return 0;
        }
        PowerNetworkSnapshot snapshot = network.createSnapshot();
        return snapshot.getTotalProductionRate();
    }

    @Override
    public double getNetworkConsumption(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        if (network == null) {
            return 0;
        }
        PowerNetworkSnapshot snapshot = network.createSnapshot();
        return snapshot.getTotalConsumptionRate();
    }

    @Override
    public double getNetworkStorage(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        if (network == null) {
            return 0;
        }
        PowerNetworkSnapshot snapshot = network.createSnapshot();
        return snapshot.getTotalStoredEnergy();
    }

    @Override
    public double getNetworkCapacity(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        if (network == null) {
            return 0;
        }
        PowerNetworkSnapshot snapshot = network.createSnapshot();
        return snapshot.getTotalStorageCapacity();
    }

    @Override
    public PowerNetworkStats getNetworkStats(UUID networkId) {
        PowerNetwork network = networks.get(networkId);
        return network != null ? network.getLastTickStats() : null;
    }

    @Override
    public void addListener(PowerNetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PowerNetworkListener listener) {
        listeners.remove(listener);
    }

    /**
     * Ticks all networks. Called by the PowerTicker.
     */
    public void tickAll() {
        for (PowerNetwork network : networks.values()) {
            network.tick();

            // Notify listeners of tick stats
            UUID networkId = network.getNetworkId();
            PowerNetworkStats stats = network.getLastTickStats();
            for (PowerNetworkListener listener : listeners) {
                listener.onNetworkTick(networkId, stats);
            }
        }
    }

    /**
     * Gets the internal network map (for debugging/testing).
     */
    public Map<UUID, PowerNetwork> getNetworks() {
        return Collections.unmodifiableMap(networks);
    }

    /**
     * Gets the total number of registered capables.
     */
    public int getTotalCapableCount() {
        return allCapables.size();
    }
}
