package net.trueHorse.wildToolAccess.commands;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

public class WildToolAccessCommands {

    public static void registerCommands(){
        WildToolAccessCommand.register(ClientCommandManager.DISPATCHER);
    }
}
