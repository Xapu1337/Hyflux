package io.fabrica.ui;

import io.fabrica.api.power.IPowerNetworkManager;
import io.fabrica.machine.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles machine UI interactions.
 * This is a placeholder for Hytale's Pages system - actual UI implementation
 * depends on the final Hytale API.
 */
public class MachineUIHandler {

    private static final MachineUIHandler INSTANCE = new MachineUIHandler();

    // Track which players have which machines open
    private final Map<UUID, MachineBlock> openMachines;

    private MachineUIHandler() {
        this.openMachines = new HashMap<>();
    }

    public static MachineUIHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Opens a machine UI for a player.
     *
     * @param playerUuid the player's UUID
     * @param machine the machine to open
     */
    public void openMachineUI(UUID playerUuid, MachineBlock machine) {
        // Close any existing open machine
        closeMachineUI(playerUuid);

        openMachines.put(playerUuid, machine);

        // In actual implementation, this would open a Hytale Page
        // For now, we just track the state
    }

    /**
     * Closes the machine UI for a player.
     *
     * @param playerUuid the player's UUID
     */
    public void closeMachineUI(UUID playerUuid) {
        openMachines.remove(playerUuid);
    }

    /**
     * Gets the machine a player has open, if any.
     *
     * @param playerUuid the player's UUID
     * @return the open machine, or null
     */
    public MachineBlock getOpenMachine(UUID playerUuid) {
        return openMachines.get(playerUuid);
    }

    /**
     * Checks if a player has a machine UI open.
     *
     * @param playerUuid the player's UUID
     * @return true if a machine is open
     */
    public boolean hasMachineOpen(UUID playerUuid) {
        return openMachines.containsKey(playerUuid);
    }

    /**
     * Gets UI data for a generator.
     *
     * @param generator the generator machine
     * @return map of UI data
     */
    public Map<String, Object> getGeneratorUIData(GeneratorMachine generator) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "generator");
        data.put("fuelItem", generator.getFuelSlot().getItemId());
        data.put("fuelCount", generator.getFuelSlot().getCount());
        data.put("storedEnergy", generator.getStoredEnergy());
        data.put("maxBuffer", GeneratorMachine.INTERNAL_BUFFER);
        data.put("burnProgress", generator.getBurnProgress());
        data.put("powerOutput", IPowerNetworkManager.formatPower(generator.getCurrentProductionRate()));
        data.put("isActive", generator.isActive());
        return data;
    }

    /**
     * Gets UI data for a battery.
     *
     * @param battery the battery machine
     * @return map of UI data
     */
    public Map<String, Object> getBatteryUIData(BatteryMachine battery) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "battery");
        data.put("storedEnergy", battery.getStoredEnergy());
        data.put("maxCapacity", battery.getMaxCapacity());
        data.put("chargePercentage", battery.getChargePercentage() * 100);
        data.put("energyDisplay", IPowerNetworkManager.formatEnergy(battery.getStoredEnergy()));
        data.put("capacityDisplay", IPowerNetworkManager.formatEnergy(battery.getMaxCapacity()));
        data.put("isActive", battery.isActive());
        return data;
    }

    /**
     * Gets UI data for a processing machine (Macerator, Electric Furnace).
     *
     * @param machine the processing machine
     * @return map of UI data
     */
    public Map<String, Object> getProcessingUIData(ProcessingMachine machine) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", machine.getMachineId());
        data.put("inputItem", machine.getInputSlot().getItemId());
        data.put("inputCount", machine.getInputSlot().getCount());
        data.put("outputItem", machine.getOutputSlot().getItemId());
        data.put("outputCount", machine.getOutputSlot().getCount());
        data.put("progress", machine.getProgress() * 100);
        data.put("energyBuffer", machine.getEnergyBuffer());
        data.put("maxEnergyBuffer", machine.getMaxEnergyBuffer());
        data.put("powerConsumption", IPowerNetworkManager.formatPower(machine.getConsumptionRate()));
        data.put("isActive", machine.isActive());

        Recipe recipe = machine.getCurrentRecipe();
        if (recipe != null) {
            data.put("recipeName", recipe.getId());
            data.put("recipeOutput", recipe.getOutputItemId());
            data.put("recipeOutputCount", recipe.getOutputCount());
        }

        return data;
    }

    /**
     * Gets UI data for any machine.
     *
     * @param machine the machine
     * @return map of UI data
     */
    public Map<String, Object> getMachineUIData(MachineBlock machine) {
        if (machine instanceof GeneratorMachine generator) {
            return getGeneratorUIData(generator);
        } else if (machine instanceof BatteryMachine battery) {
            return getBatteryUIData(battery);
        } else if (machine instanceof ProcessingMachine processing) {
            return getProcessingUIData(processing);
        }

        // Generic machine data
        Map<String, Object> data = new HashMap<>();
        data.put("type", machine.getMachineId());
        data.put("position", machine.getPosition().toString());
        data.put("isActive", machine.isActive());
        return data;
    }
}
