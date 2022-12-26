package net.trueHorse.wildToolAccess.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.*;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgument;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WildToolAccessCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
        dispatcher.register(literal("wta")
                .then(literal("stuffTag")
                        .then(literal("add")
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(argument("type", new AccessTypeArgumentType(registryAccess)).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.ADD, context.getSource())))
                                .then(literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(literal("remove")
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
                                .then(argument("type", new AccessTypeArgumentType(registryAccess)).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.REMOVE, context.getSource())))
                                .then(literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.REMOVE, context.getSource())))
                                .then(literal("all").executes(context->WildToolAccessCommand.executeClearStuff(context.getSource()))))
                        .then(literal("list").executes(context -> WildToolAccessCommand.executePrintStuff(context.getSource()))
                        )));
    }

    private static int executeModifyStuff(Collection<Item> items, Operation operation,FabricClientCommandSource source){

        return 1;
    }

    private static int executeClearStuff(FabricClientCommandSource source){

        return 1;
    }

    private static int executePrintStuff(FabricClientCommandSource source){

        return 1;
    }

    private static ArrayList<Item> getItemListFromItemArgument(CommandContext<FabricClientCommandSource> context){
        ArrayList<Item> list = new ArrayList<Item>();
        list.add(context.getArgument("item",ItemStackArgument.class).getItem());
        return list;
    }

    private static ArrayList<Item> getItemListFromAccessTypeArgument(CommandContext<FabricClientCommandSource> context){
        Class<?> type = context.getArgument("type", AccessTypeArgument.class).getType();
        Set<ItemStack> set = ItemStackSet.create();
        for (ItemGroup itemGroup : ItemGroups.getGroups()) {
            if (itemGroup.getType() == ItemGroup.Type.SEARCH) continue;
            set.addAll(itemGroup.getSearchTabStacks());
        }

        ArrayList<Item> items = new ArrayList<Item>();
        for(ItemStack stack:set){
            if(type.isAssignableFrom(stack.getItem().getClass())){
                items.add(stack.getItem());
            }
        }

        return items;
    }

    private static ArrayList<Item> getItemListFromInventory(CommandContext<FabricClientCommandSource> context){
        ArrayList<Item> items = new ArrayList<Item>();
        context.getSource().getClient().player.getInventory().main.forEach(stack->items.add(stack.getItem()));
        return items;
    }

    private enum Operation{
        ADD{
            @Override
            protected boolean applyModification() {
                return false;
            }
        },
        REMOVE{
            @Override
            protected boolean applyModification() {
                return false;
            }
        };

        protected abstract boolean applyModification();
    }

}
