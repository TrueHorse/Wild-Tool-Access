package net.trueHorse.wildToolAccess.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.ItemTypeHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.function.Function;

public class WildToolAccessCommand {

    private static final Function<String,SimpleCommandExceptionType> STUFF_FILE_NOT_FOUND = (typeName)->new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.file_not_found",typeName));
    private static final Function<String, SimpleCommandExceptionType> COULDNT_WRITE_TO_STUFF = (typeName)->new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.couldnt_write",typeName));
    private static final Function<String, SimpleCommandExceptionType> PROB_COULDNT_READ_STUFF = (typeName)->new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.probably_couldnt_read",typeName));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        dispatcher.register(Commands.literal("wta")
                .then(Commands.argument("itemType", StringArgumentType.word())
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyItemType(context, WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType()).suggests((context, builder) -> {
                                    for(String itemType : ItemTypeHandler.getItemTypes()){
                                        builder.suggest(itemType);
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyItemType(context, WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType()).suggests((context,builder) -> {
                                    for(String itemType : ItemTypeHandler.getItemTypes()){
                                        builder.suggest(itemType);
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromInventory(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("all").executes(WildToolAccessCommand::executeClearItemType)))
                        .then(Commands.literal("list").executes(WildToolAccessCommand::executePrintItemType))
                ));
    }

    private static int executeModifyItemType(CommandContext<CommandSourceStack> context, ArrayList<ResourceLocation> itemIds, Operation operation, CommandSourceStack source) throws CommandSyntaxException {
        String itemType = context.getArgument("itemType",String.class);
        File typeFile = ItemTypeHandler.ITEM_TYPE_DIRECTORY.toPath().resolve(itemType+".json").toFile();
        if(!typeFile.exists()){
            ItemTypeHandler.createEmptyItemType(itemType);
        }

        JsonObject obj;
        try {
            obj = GsonHelper.parse(new FileReader(typeFile));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.apply(itemType).create();
        }

        JsonArray vals = GsonHelper.getAsJsonArray(obj,"values");
        Component feedback = operation.apply(itemIds,vals,source,itemType);

        try{
            FileWriter fwriter = new FileWriter(typeFile);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendSuccess(()-> feedback,false);
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.apply(itemType).create();
        } catch (IOException e) {
            throw COULDNT_WRITE_TO_STUFF.apply(itemType).create();
        } catch (Exception e){
           throw PROB_COULDNT_READ_STUFF.apply(itemType).create();
        }

        ItemTypeHandler.loadItemTypes(source.registryAccess());
        return 1;
    }

    private static int executeClearItemType(CommandContext<CommandSourceStack> context){
        String typeName = context.getArgument("itemType",String.class);

        ItemTypeHandler.createEmptyItemType(typeName);
        ItemTypeHandler.loadItemTypes(context.getSource().registryAccess());
        context.getSource().sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared",typeName),false);
        return 1;
    }

    private static int executePrintItemType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String typeName = context.getArgument("itemType",String.class);
        File typeFile = ItemTypeHandler.ITEM_TYPE_DIRECTORY.toPath().resolve(typeName+".json").toFile();

        if(!typeFile.exists()){
            context.getSource().sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff",typeName),false);
        }else{
            try {
                JsonObject obj = GsonHelper.parse(new FileReader(typeFile));
                JsonArray vals = GsonHelper.getAsJsonArray(obj, "values");
                if(vals.isEmpty()){
                    context.getSource().sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff",typeName),false);
                }

                vals.forEach(val->context.getSource().sendSuccess(()->Component.literal(val.getAsString()),false));
            } catch (FileNotFoundException e) {
                throw STUFF_FILE_NOT_FOUND.apply(typeName).create();
            }
        }
        return 1;
    }

    private static ArrayList<ResourceLocation> getItemListFromItemArgument(CommandContext<CommandSourceStack> context) {
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>();
        list.add(BuiltInRegistries.ITEM.getKey(context.getArgument("item", ItemInput.class).getItem()));
        return list;
    }

    private static ArrayList<ResourceLocation> getItemListFromAccessTypeArgument(CommandContext<CommandSourceStack> context){
        return new ArrayList<>(ItemTypeHandler.getItemType(context.getArgument("type", String.class)).stream().map(BuiltInRegistries.ITEM::getKey).toList());
    }

    private static ArrayList<ResourceLocation> getItemListFromInventory(CommandContext<CommandSourceStack> context){
        ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
        Minecraft.getInstance().player.getInventory().items.forEach(stack-> {
            if(!stack.isEmpty()){
                ids.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        } );
        return ids;
    }

    private enum Operation{
        ADD{
            @Override
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source, String typeName) {
                ArrayList<ResourceLocation> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.already_contains", id.toString(),typeName),false);
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return Component.translatable("command.wildtoolaccess.stuff.added",addedIds,typeName);
            }
        },
        REMOVE{
            @Override
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source, String typeName) {
                ArrayList<ResourceLocation> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.does_not_contain",id.toString(),typeName),false);
                        removedIds.remove(id);
                    }
                });

                return Component.translatable("command.wildtoolaccess.stuff.removed",removedIds,typeName);
            }
        };

        protected abstract Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSourceStack source, String typeName);
    }

}
