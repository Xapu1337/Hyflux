package io.fabrica.power;

import io.fabrica.api.power.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a connected group of power-capable blocks.
 * Handles the internal state and energy distribution for a network.
 */
public class PowerNetwork {

    private final UUID networkId;
    private final Map<BlockPos, IPowerCapable> members;
    private final List<IPowerProducer> producers;
    private final List<IPowerConsumer> consumers;
    private final List<IPowerStorage> storages;
    private final List<IPowerConduit> conduits;

    private PowerNetworkStats lastTickStats;
    private boolean dirty;

    public PowerNetwork() {
        this.networkId = UUID.randomUUID();
        this.members = new ConcurrentHashMap<>();
        this.producers = Collections.synchronizedList(new ArrayList<>());
        this.consumers = Collections.synchronizedList(new ArrayList<>());
        this.storages = Collections.synchronizedList(new ArrayList<>());
        this.conduits = Collections.synchronizedList(new ArrayList<>());
        this.lastTickStats = new PowerNetworkStats(0, 0, 0, 0, 0, 1.0);
        this.dirty = false;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    /**
     * Adds a power-capable block to this network.
     */
    public void addMember(IPowerCapable capable) {
        BlockPos pos = capable.getPosition();
        if (members.containsKey(pos)) {
            return;
        }

        members.put(pos, capable);
        capable.setNetworkId(networkId);

        // Add to appropriate list based on capability
        if (capable instanceof IPowerProducer producer) {
            producers.add(producer);
        }
        if (capable instanceof IPowerConsumer consumer) {
            consumers.add(consumer);
        }
        if (capable instanceof IPowerStorage storage) {
            storages.add(storage);
        }
        if (capable instanceof IPowerConduit conduit) {
            conduits.add(conduit);
        }

        dirty = true;
    }

    /**
     * Removes a power-capable block from this network.
     */
    public void removeMember(IPowerCapable capable) {
        BlockPos pos = capable.getPosition();
        if (!members.containsKey(pos)) {
            return;
        }

        members.remove(pos);
        capable.setNetworkId(null);

        // Remove from appropriate lists
        if (capable instanceof IPowerProducer producer) {
            producers.remove(producer);
        }
        if (capable instanceof IPowerConsumer consumer) {
            consumers.remove(consumer);
        }
        if (capable instanceof IPowerStorage storage) {
            storages.remove(storage);
        }
        if (capable instanceof IPowerConduit conduit) {
            conduits.remove(conduit);
        }

        dirty = true;
    }

    /**
     * Checks if this network contains the given position.
     */
    public boolean containsPosition(BlockPos pos) {
        return members.containsKey(pos);
    }

    /**
     * Gets the member at the given position.
     */
    public IPowerCapable getMemberAt(BlockPos pos) {
        return members.get(pos);
    }

    /**
     * Gets all member positions.
     */
    public Set<BlockPos> getMemberPositions() {
        return new HashSet<>(members.keySet());
    }

    /**
     * Gets the number of members in this network.
     */
    public int size() {
        return members.size();
    }

    /**
     * Checks if this network is empty.
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Processes one tick of power distribution.
     *
     * Distribution algorithm:
     * 1. Calculate total production and demand
     * 2. Producers feed consumers directly
     * 3. Excess production charges storage
     * 4. Deficit draws from storage
     * 5. If still deficit, consumers operate at reduced capacity
     */
    public void tick() {
        // Calculate production capacity
        double totalProductionCapacity = 0;
        for (IPowerProducer producer : producers) {
            if (producer.isProducing()) {
                totalProductionCapacity += IPowerNetworkManager.wattsToJoulesPerTick(producer.getCurrentProductionRate());
            }
        }

        // Calculate consumption demand
        double totalDemand = 0;
        for (IPowerConsumer consumer : consumers) {
            if (consumer.canOperate()) {
                totalDemand += IPowerNetworkManager.wattsToJoulesPerTick(consumer.getConsumptionRate());
            }
        }

        // Calculate available storage discharge
        double availableFromStorage = 0;
        for (IPowerStorage storage : storages) {
            availableFromStorage += Math.min(
                storage.getStoredEnergy(),
                IPowerNetworkManager.wattsToJoulesPerTick(storage.getMaxDischargeRate())
            );
        }

        // Calculate storage charge capacity
        double storageChargeCapacity = 0;
        for (IPowerStorage storage : storages) {
            double availableCapacity = storage.getMaxCapacity() - storage.getStoredEnergy();
            storageChargeCapacity += Math.min(
                availableCapacity,
                IPowerNetworkManager.wattsToJoulesPerTick(storage.getMaxChargeRate())
            );
        }

        // Step 1: Produce energy
        double energyProduced = 0;
        for (IPowerProducer producer : producers) {
            if (producer.isProducing()) {
                double maxProduce = IPowerNetworkManager.wattsToJoulesPerTick(producer.getCurrentProductionRate());
                energyProduced += producer.produce(maxProduce);
            }
        }

        // Step 2: Calculate how much energy is available for consumers
        double totalAvailable = energyProduced + availableFromStorage;
        double satisfactionRatio = totalDemand > 0 ? Math.min(1.0, totalAvailable / totalDemand) : 1.0;

        // Step 3: Distribute to consumers
        double energyConsumed = 0;
        for (IPowerConsumer consumer : consumers) {
            if (consumer.canOperate()) {
                double requested = IPowerNetworkManager.wattsToJoulesPerTick(consumer.getConsumptionRate());
                double available = requested * satisfactionRatio;
                energyConsumed += consumer.consume(available);
            }
        }

        // Step 4: Handle excess/deficit
        double energyCharged = 0;
        double energyDischarged = 0;
        double energyWasted = 0;

        double surplus = energyProduced - energyConsumed;

        if (surplus > 0) {
            // Excess energy - charge storage
            double toCharge = surplus;
            for (IPowerStorage storage : storages) {
                if (toCharge <= 0) break;
                double availableCapacity = storage.getMaxCapacity() - storage.getStoredEnergy();
                double maxCharge = Math.min(
                    availableCapacity,
                    IPowerNetworkManager.wattsToJoulesPerTick(storage.getMaxChargeRate())
                );
                double overflow = storage.charge(Math.min(toCharge, maxCharge));
                double charged = Math.min(toCharge, maxCharge) - overflow;
                energyCharged += charged;
                toCharge -= charged;
            }
            energyWasted = toCharge; // Anything left over is wasted
        } else if (surplus < 0) {
            // Deficit - discharge storage
            double needed = -surplus;
            for (IPowerStorage storage : storages) {
                if (needed <= 0) break;
                double maxDischarge = Math.min(
                    storage.getStoredEnergy(),
                    IPowerNetworkManager.wattsToJoulesPerTick(storage.getMaxDischargeRate())
                );
                double discharged = storage.discharge(Math.min(needed, maxDischarge));
                energyDischarged += discharged;
                needed -= discharged;
            }
        }

        // Update stats
        lastTickStats = new PowerNetworkStats(
            energyProduced,
            energyConsumed,
            energyCharged,
            energyDischarged,
            energyWasted,
            satisfactionRatio
        );
    }

    /**
     * Gets the stats from the last tick.
     */
    public PowerNetworkStats getLastTickStats() {
        return lastTickStats;
    }

    /**
     * Creates a snapshot of the current network state.
     */
    public PowerNetworkSnapshot createSnapshot() {
        double totalProduction = 0;
        double totalConsumption = 0;
        double totalStored = 0;
        double totalCapacity = 0;

        for (IPowerProducer producer : producers) {
            totalProduction += producer.getCurrentProductionRate();
        }

        for (IPowerConsumer consumer : consumers) {
            if (consumer.canOperate()) {
                totalConsumption += consumer.getConsumptionRate();
            }
        }

        for (IPowerStorage storage : storages) {
            totalStored += storage.getStoredEnergy();
            totalCapacity += storage.getMaxCapacity();
        }

        return new PowerNetworkSnapshot(
            networkId,
            getMemberPositions(),
            producers.size(),
            consumers.size(),
            storages.size(),
            conduits.size(),
            totalProduction,
            totalConsumption,
            totalStored,
            totalCapacity
        );
    }

    /**
     * Merges another network into this one.
     * All members from the other network are transferred here.
     */
    public void merge(PowerNetwork other) {
        for (IPowerCapable capable : other.members.values()) {
            addMember(capable);
        }
    }

    /**
     * Checks if this network is connected (all members reachable from any starting point).
     * Uses BFS to traverse connections.
     */
    public boolean isConnected() {
        if (members.isEmpty()) {
            return true;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Start from any member
        BlockPos start = members.keySet().iterator().next();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            IPowerCapable capable = members.get(current);

            // Check all adjacent positions
            for (BlockPos adjacent : current.getAdjacent()) {
                if (members.containsKey(adjacent) && !visited.contains(adjacent)) {
                    visited.add(adjacent);
                    queue.add(adjacent);
                }
            }

            // For conduits, also check their explicit connections
            if (capable instanceof IPowerConduit conduit) {
                for (BlockPos connected : conduit.getConnectedPositions()) {
                    if (members.containsKey(connected) && !visited.contains(connected)) {
                        visited.add(connected);
                        queue.add(connected);
                    }
                }
            }
        }

        return visited.size() == members.size();
    }

    /**
     * Splits this network into connected components.
     * Returns a list of new networks if split occurred, or just this network if still connected.
     */
    public List<PowerNetwork> splitIntoComponents() {
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        List<PowerNetwork> components = new ArrayList<>();
        Set<BlockPos> unvisited = new HashSet<>(members.keySet());

        while (!unvisited.isEmpty()) {
            PowerNetwork component = new PowerNetwork();
            Queue<BlockPos> queue = new LinkedList<>();

            // Start from any unvisited member
            BlockPos start = unvisited.iterator().next();
            queue.add(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                if (!unvisited.contains(current)) {
                    continue;
                }

                unvisited.remove(current);
                IPowerCapable capable = members.get(current);
                component.addMember(capable);

                // Check all adjacent positions
                for (BlockPos adjacent : current.getAdjacent()) {
                    if (unvisited.contains(adjacent)) {
                        queue.add(adjacent);
                    }
                }

                // For conduits, also check their explicit connections
                if (capable instanceof IPowerConduit conduit) {
                    for (BlockPos connected : conduit.getConnectedPositions()) {
                        if (unvisited.contains(connected)) {
                            queue.add(connected);
                        }
                    }
                }
            }

            components.add(component);
        }

        return components;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
