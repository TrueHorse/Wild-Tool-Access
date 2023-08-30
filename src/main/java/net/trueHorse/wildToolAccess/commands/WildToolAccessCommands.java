package net.trueHorse.wildToolAccess.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public class WildToolAccessCommands {

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher){
        WildToolAccessCommand.register(dispatcher);
    }
}
