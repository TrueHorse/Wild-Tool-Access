package net.trueHorse.wildToolAccess.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgument;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.StuffHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class WildToolAccessCommand {

    private static final SimpleCommandExceptionType STUFF_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.file_not_found"));
    private static final SimpleCommandExceptionType COULDNT_WRITE_TO_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.couldnt_write"));
    private static final SimpleCommandExceptionType PROB_COULDNT_READ_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.probably_couldnt_read"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        dispatcher.register(Commands.literal("wta")
                .then(Commands.literal("stuff")
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType()).suggests((context, builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType()).suggests((context,builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("all").executes(context->WildToolAccessCommand.executeClearStuff(context.getSource()))))
                        .then(Commands.literal("list").executes(context -> WildToolAccessCommand.executePrintStuff(context.getSource())))
                        .then(Commands.literal("reset").executes(context -> WildToolAccessCommand.executeResetStuff(context.getSource())))
                ));
    }

    private static int executeModifyStuff(ArrayList<ResourceLocation> itemIds, Operation operation, CommandSourceStack source) throws CommandSyntaxException {
        if(!StuffHandler.STUFF_FILE.exists()){
            StuffHandler.createStuffFileWithValuesEmpty();
        }

        JsonObject obj;
        try {
            obj = GsonHelper.parse(new FileReader(StuffHandler.STUFF_FILE));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        }

        JsonArray vals = GsonHelper.getAsJsonArray(obj,"values");
        Component feedback = operation.apply(itemIds,vals,source);

        try{
            FileWriter fwriter = new FileWriter(StuffHandler.STUFF_FILE);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendSuccess(()-> feedback,false);
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        } catch (IOException e) {
            throw COULDNT_WRITE_TO_STUFF.create();
        } catch (Exception e){
           throw PROB_COULDNT_READ_STUFF.create();
        }

        StuffHandler.loadStuffItems();
        return 1;
    }

    private static int executeClearStuff(CommandSourceStack source){
        StuffHandler.createStuffFileWithValuesEmpty();
        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared"),false);
        return 1;
    }

    private static int executeResetStuff(CommandSourceStack source){
        StuffHandler.resetStuffFile();
        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared"),false);
        return 1;
    }

    private static int executePrintStuff(CommandSourceStack source) throws CommandSyntaxException {
        if(!StuffHandler.STUFF_FILE.exists()){
            source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff"),false);
        }else{
            try {
                JsonObject obj = GsonHelper.parse(new FileReader(StuffHandler.STUFF_FILE));
                JsonArray vals = GsonHelper.getAsJsonArray(obj, "values");
                if(vals.isEmpty()){
                    source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff"),false);
                }

                vals.forEach(val->source.sendSuccess(()->Component.literal(val.getAsString()),false));
            } catch (FileNotFoundException e) {
                throw STUFF_FILE_NOT_FOUND.create();
            }
        }
        return 1;
    }

    private static ArrayList<ResourceLocation> getItemListFromItemArgument(CommandContext<CommandSourceStack> context) {
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>();
        list.add(ForgeRegistries.ITEMS.getKey(context.getArgument("item", ItemInput.class).getItem()));
        return list;
    }

    private static ArrayList<ResourceLocation> getItemListFromAccessTypeArgument(CommandContext<CommandSourceStack> context){
        ArrayList<ResourceLocation> itemIdsOfType = new ArrayList<ResourceLocation>();
        Class<?> type = context.getArgument("type", AccessTypeArgument.class).getType();

        if(type == StuffPlaceholder.class){
            itemIdsOfType.addAll(StuffHandler.getStuffItems().stream().map(ForgeRegistries.ITEMS::getKey).toList());
        }else{
            Set<ResourceLocation> allItemIds = ForgeRegistries.ITEMS.getKeys();
            for(ResourceLocation id:allItemIds){
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if(type.isAssignableFrom(item.getClass())){
                    itemIdsOfType.add(id);
                }
            }
        }

        return itemIdsOfType;
    }

    private static ArrayList<ResourceLocation> getItemListFromInventory(CommandContext<CommandSourceStack> context){
        ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
        Minecraft.getInstance().player.getInventory().items.forEach(stack-> {
            if(!stack.isEmpty()){
                ids.add(ForgeRegistries.ITEMS.getKey(stack.getItem()));
            }
        } );
        return ids;
    }

    private enum Operation{
        ADD{
            @Override
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source) {
                ArrayList<ResourceLocation> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.already_contains", id.toString()),false);
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return Component.translatable("command.wildtoolaccess.stuff.added",addedIds);
            }
        },
        REMOVE{
            @Override
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source) {
                ArrayList<ResourceLocation> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.does_not_contain",id.toString()),false);
                        removedIds.remove(id);
                    }
                });

                return Component.translatable("command.wildtoolaccess.stuff.removed",removedIds);
            }
        };

        protected abstract Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source);
    }

}
