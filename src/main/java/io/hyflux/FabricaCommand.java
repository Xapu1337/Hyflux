
package io.fabrica;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;

public class FabricaCommand extends CommandBase {

    private final String pluginName;
    private final String pluginVersion;

    public FabricaCommand(String pluginName, String pluginVersion) {
        super("fabrica", "Prints info about the Fabrica plugin.");
        this.setPermissionGroup(GameMode.Adventure);
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("Hello from " + pluginName + " v" + pluginVersion + "!"));
    }
}
