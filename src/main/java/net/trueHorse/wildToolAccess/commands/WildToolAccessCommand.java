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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.ItemTypeHandler;

import java.io.*;
import java.util.ArrayList;

public class WildToolAccessCommand {

    private static final SimpleCommandExceptionType STUFF_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.file_not_found"));
    private static final SimpleCommandExceptionType COULDNT_WRITE_TO_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.couldnt_write"));
    private static final SimpleCommandExceptionType PROB_COULDNT_READ_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.probably_couldnt_read"));

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
                        .then(Commands.literal("list").executes(context -> WildToolAccessCommand.executePrintItemType(context.getSource())))
                ));
    }

    private static int executeModifyItemType(CommandContext<CommandSourceStack> context, ArrayList<ResourceLocation> itemIds, Operation operation, CommandSourceStack source) throws CommandSyntaxException {
        String itemType = context.getArgument("itemType",String.class);
        if(!ItemTypeHandler.ITEM_TYPE_DIRECTORY.toPath().resolve(itemType).toFile().exists()){
            ItemTypeHandler.createEmptyItemType(itemType);
        }

        JsonObject obj;
        try {
            obj = GsonHelper.parse(new FileReader(ItemTypeHandler.ITEM_TYPE_DIRECTORY));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        }

        JsonArray vals = GsonHelper.getAsJsonArray(obj,"values");
        Component feedback = operation.apply(itemIds,vals,source);

        try{
            FileWriter fwriter = new FileWriter(ItemTypeHandler.ITEM_TYPE_DIRECTORY);
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

        ItemTypeHandler.loadItemTypes(source.registryAccess());
        return 1;
    }

    private static int executeClearItemType(CommandContext<CommandSourceStack> context){
        ItemTypeHandler.createEmptyItemType(context.getArgument("itemType",String.class));
        ItemTypeHandler.loadItemTypes(context.getSource().registryAccess());
        context.getSource().sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared"),false);
        return 1;
    }

    private static int executePrintItemType(CommandSourceStack source) throws CommandSyntaxException {
        if(!ItemTypeHandler.ITEM_TYPE_DIRECTORY.exists()){
            source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff"),false);
        }else{
            try {
                JsonObject obj = GsonHelper.parse(new FileReader(ItemTypeHandler.ITEM_TYPE_DIRECTORY));
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
        return new ArrayList<>(ItemTypeHandler.getItemType(context.getArgument("type", String.class)).stream().map(ForgeRegistries.ITEMS::getKey).toList());
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
