package io.fabrica.machine;

import io.fabrica.api.power.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Registry that tracks all placed machines in the world.
 * Provides lookup by position and type.
 */
public class MachineRegistry {

    private static final MachineRegistry INSTANCE = new MachineRegistry();

    private final Map<BlockPos, MachineBlock> machinesByPosition;
    private final Map<String, Set<MachineBlock>> machinesByType;

    private MachineRegistry() {
        this.machinesByPosition = new ConcurrentHashMap<>();
        this.machinesByType = new ConcurrentHashMap<>();
    }

    public static MachineRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a machine at a position.
     *
     * @param machine the machine to register
     */
    public void register(MachineBlock machine) {
        BlockPos pos = machine.getPosition();

        // Remove any existing machine at this position
        unregister(pos);

        machinesByPosition.put(pos, machine);
        machinesByType.computeIfAbsent(machine.getMachineId(), k -> ConcurrentHashMap.newKeySet())
            .add(machine);

        machine.onPlace();
    }

    /**
     * Unregisters the machine at a position.
     *
     * @param pos the position to unregister
     * @return the removed machine, or null if none
     */
    public MachineBlock unregister(BlockPos pos) {
        MachineBlock machine = machinesByPosition.remove(pos);
        if (machine != null) {
            Set<MachineBlock> typeSet = machinesByType.get(machine.getMachineId());
            if (typeSet != null) {
                typeSet.remove(machine);
            }
            machine.onBreak();
        }
        return machine;
    }

    /**
     * Gets the machine at a position.
     *
     * @param pos the position to check
     * @return the machine, or null if none
     */
    public MachineBlock getMachineAt(BlockPos pos) {
        return machinesByPosition.get(pos);
    }

    /**
     * Gets the machine at a position, cast to a specific type.
     *
     * @param pos the position to check
     * @param type the expected machine class
     * @return the machine, or null if none or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T extends MachineBlock> T getMachineAt(BlockPos pos, Class<T> type) {
        MachineBlock machine = machinesByPosition.get(pos);
        if (machine != null && type.isInstance(machine)) {
            return (T) machine;
        }
        return null;
    }

    /**
     * Checks if there's a machine at a position.
     *
     * @param pos the position to check
     * @return true if a machine exists
     */
    public boolean hasMachineAt(BlockPos pos) {
        return machinesByPosition.containsKey(pos);
    }

    /**
     * Gets all machines of a specific type.
     *
     * @param machineId the machine type ID
     * @return unmodifiable set of machines
     */
    public Set<MachineBlock> getMachinesOfType(String machineId) {
        Set<MachineBlock> machines = machinesByType.get(machineId);
        return machines != null ? Collections.unmodifiableSet(machines) : Collections.emptySet();
    }

    /**
     * Gets all registered machines.
     *
     * @return unmodifiable collection of all machines
     */
    public Collection<MachineBlock> getAllMachines() {
        return Collections.unmodifiableCollection(machinesByPosition.values());
    }

    /**
     * Gets machines within a certain distance of a position.
     *
     * @param center the center position
     * @param radius the search radius (Manhattan distance)
     * @return list of nearby machines
     */
    public List<MachineBlock> getMachinesNear(BlockPos center, int radius) {
        List<MachineBlock> result = new ArrayList<>();
        for (MachineBlock machine : machinesByPosition.values()) {
            if (center.manhattanDistance(machine.getPosition()) <= radius) {
                result.add(machine);
            }
        }
        return result;
    }

    /**
     * Gets machines matching a predicate.
     *
     * @param predicate the filter condition
     * @return list of matching machines
     */
    public List<MachineBlock> getMachinesMatching(Predicate<MachineBlock> predicate) {
        List<MachineBlock> result = new ArrayList<>();
        for (MachineBlock machine : machinesByPosition.values()) {
            if (predicate.test(machine)) {
                result.add(machine);
            }
        }
        return result;
    }

    /**
     * Ticks all registered machines.
     */
    public void tickAll() {
        for (MachineBlock machine : machinesByPosition.values()) {
            machine.tick();
        }
    }

    /**
     * Gets the total number of registered machines.
     */
    public int getMachineCount() {
        return machinesByPosition.size();
    }

    /**
     * Gets the number of machines of a specific type.
     */
    public int getMachineCount(String machineId) {
        Set<MachineBlock> machines = machinesByType.get(machineId);
        return machines != null ? machines.size() : 0;
    }

    /**
     * Clears all registered machines.
     */
    public void clear() {
        for (MachineBlock machine : new ArrayList<>(machinesByPosition.values())) {
            machine.onBreak();
        }
        machinesByPosition.clear();
        machinesByType.clear();
    }

    /**
     * Gets a summary of all registered machine types and counts.
     */
    public Map<String, Integer> getMachineSummary() {
        Map<String, Integer> summary = new HashMap<>();
        for (Map.Entry<String, Set<MachineBlock>> entry : machinesByType.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }
        return summary;
    }
}
