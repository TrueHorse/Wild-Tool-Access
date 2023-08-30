package net.trueHorse.wildToolAccess.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class WildToolAccessCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher){
        WildToolAccessCommand.register(dispatcher);
    }
}
