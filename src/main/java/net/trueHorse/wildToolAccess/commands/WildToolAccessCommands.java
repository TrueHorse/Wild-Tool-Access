package net.trueHorse.wildToolAccess.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class WildToolAccessCommands {

    public static void registerCommands(){
        ClientCommandRegistrationCallback.EVENT.register((WildToolAccessCommand::register));
    }
}
