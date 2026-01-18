package io.fabrica.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import io.fabrica.api.power.IPowerNetworkManager;
import io.fabrica.api.power.PowerNetworkSnapshot;
import io.fabrica.power.PowerNetworkManagerImpl;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Debug command to display power network information.
 * Usage: /fpower [networkIndex]
 */
public class PowerCommand extends CommandBase {

    private final PowerNetworkManagerImpl powerManager;

    public PowerCommand(@Nonnull PowerNetworkManagerImpl powerManager) {
        super("fpower", "Shows Fabrica power network information");
        this.setPermissionGroup(GameMode.Creative);  // Only creative/OP players
        this.powerManager = powerManager;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        Collection<PowerNetworkSnapshot> networks = powerManager.getAllNetworks();

        if (networks.isEmpty()) {
            ctx.sendMessage(Message.raw("[Fabrica] No active power networks"));
            return;
        }

        ctx.sendMessage(Message.raw("[Fabrica] Power Networks: " + networks.size()));
        ctx.sendMessage(Message.raw("----------------------------"));

        int index = 0;
        for (PowerNetworkSnapshot network : networks) {
            ctx.sendMessage(Message.raw(String.format(
                "[%d] Production: %s | Consumption: %s",
                index++,
                IPowerNetworkManager.formatPower(network.getTotalProductionRate()),
                IPowerNetworkManager.formatPower(network.getTotalConsumptionRate())
            )));

            ctx.sendMessage(Message.raw(String.format(
                "    Storage: %s / %s (%.1f%%)",
                IPowerNetworkManager.formatEnergy(network.getTotalStoredEnergy()),
                IPowerNetworkManager.formatEnergy(network.getTotalStorageCapacity()),
                network.getTotalStorageCapacity() > 0 ?
                    (network.getTotalStoredEnergy() / network.getTotalStorageCapacity() * 100) : 0
            )));

            ctx.sendMessage(Message.raw(String.format(
                "    Members: %d producers, %d consumers, %d storage, %d conduits",
                network.getProducerCount(),
                network.getConsumerCount(),
                network.getStorageCount(),
                network.getConduitCount()
            )));
        }
    }
}
