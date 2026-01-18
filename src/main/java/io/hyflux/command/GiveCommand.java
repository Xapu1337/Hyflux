package io.fabrica.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Debug command for listing Fabrica items.
 * Usage: /fgive
 *
 * Note: Actual item giving requires Hytale's inventory API which
 * may not be fully available. This command lists available items.
 */
public class GiveCommand extends CommandBase {

    // Valid Fabrica items
    private static final Set<String> VALID_ITEMS = new HashSet<>(Arrays.asList(
        // Ores
        "Tin_Ore", "Copper_Ore",
        // Dusts
        "Iron_Dust", "Copper_Dust", "Tin_Dust", "Bronze_Dust",
        // Ingots
        "Tin_Ingot", "Copper_Ingot", "Bronze_Ingot",
        // Machines
        "Machine_Generator", "Machine_Battery", "Machine_Macerator",
        "Machine_Electric_Furnace", "Machine_Cable", "Conveyor_Belt"
    ));

    public GiveCommand() {
        super("fgive", "Lists Fabrica items");
        this.setPermissionGroup(GameMode.Creative);  // Requires creative mode
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // List all available Fabrica items
        ctx.sendMessage(Message.raw("=== Fabrica Items ==="));
        ctx.sendMessage(Message.raw("--- Ores ---"));
        ctx.sendMessage(Message.raw("  Tin_Ore, Copper_Ore"));
        ctx.sendMessage(Message.raw("--- Dusts ---"));
        ctx.sendMessage(Message.raw("  Iron_Dust, Copper_Dust, Tin_Dust, Bronze_Dust"));
        ctx.sendMessage(Message.raw("--- Ingots ---"));
        ctx.sendMessage(Message.raw("  Tin_Ingot, Copper_Ingot, Bronze_Ingot"));
        ctx.sendMessage(Message.raw("--- Machines ---"));
        ctx.sendMessage(Message.raw("  Machine_Generator, Machine_Battery"));
        ctx.sendMessage(Message.raw("  Machine_Macerator, Machine_Electric_Furnace"));
        ctx.sendMessage(Message.raw("  Machine_Cable, Conveyor_Belt"));
        ctx.sendMessage(Message.raw(""));
        ctx.sendMessage(Message.raw("Use /give <item_id> to obtain items."));
    }
}
