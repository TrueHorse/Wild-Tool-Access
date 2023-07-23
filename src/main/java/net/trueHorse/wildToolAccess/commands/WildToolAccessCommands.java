package net.trueHorse.wildToolAccess.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class WildToolAccessCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        WildToolAccessCommand.register(dispatcher,buildContext);
    }
}
