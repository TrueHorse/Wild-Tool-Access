package net.trueHorse.wildToolAccess.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.function.Function;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WildToolAccessCommand {

    private static final Function<String,Text> TYPE_FILE_NOT_FOUND = ((typeName)->Text.translatable("command.wildtoolaccess.stuff.file_not_found",typeName));
    private static final Function<String,Text> COULDNT_WRITE_TO_TYPE_FILE = (typeName)->Text.translatable("command.wildtoolaccess.stuff.couldnt_write",typeName);
    private static final Function<String,Text> PROB_COULDNT_READ_TYPE_FILE = (typeName)->Text.translatable("command.wildtoolaccess.stuff.probably_couldnt_read",typeName);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
        dispatcher.register(literal("wta")
                .then(argument("itemType", StringArgumentType.word())
                        .then(literal("add")
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> executeModifyItemType(context, WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(argument("type", new AccessTypeArgumentType()).suggests((context,builder) -> {
                                    for(String itemType : WildToolAccessConfig.getItemTypes()){
                                        builder.suggest(itemType);
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromAccessTypeArgument(context,registryAccess),Operation.ADD, context.getSource())))
                                .then(literal("inventory").executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(literal("remove")
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> executeModifyItemType(context, WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
                                .then(argument("type", new AccessTypeArgumentType()).suggests((context,builder) -> {
                                    for(String itemType : WildToolAccessConfig.getItemTypes()){
                                        builder.suggest(itemType);
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromAccessTypeArgument(context,registryAccess),Operation.REMOVE, context.getSource())))
                                .then(literal("inventory").executes(context->WildToolAccessCommand.executeModifyItemType(context, WildToolAccessCommand.getItemListFromInventory(context),Operation.REMOVE, context.getSource())))
                                .then(literal("all").executes(WildToolAccessCommand::executeClearItemType)))
                        .then(literal("list").executes(WildToolAccessCommand::executePrintItemType))
                ));
    }

    private static int executeModifyItemType(CommandContext<FabricClientCommandSource> context, ArrayList<Identifier> itemIds, Operation operation, FabricClientCommandSource source){
        String itemType = context.getArgument("itemType",String.class);
        File typeFile = WildToolAccessConfig.ITEM_TYPE_DIRECTORY.toPath().resolve(itemType+".json").toFile();
        if(!typeFile.exists()){
            WildToolAccessConfig.createEmptyItemType(itemType);
        }

        JsonObject obj;
        try {
            obj = JsonHelper.deserialize(new FileReader(typeFile));
        } catch (FileNotFoundException e) {
            source.sendError(TYPE_FILE_NOT_FOUND.apply(itemType));
            return 0;
        }

        JsonArray vals = JsonHelper.getArray(obj,"values");
        Text feedback = operation.apply(itemIds,vals,source,itemType);

        try{
            FileWriter fwriter = new FileWriter(typeFile);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendFeedback(feedback);
        } catch (FileNotFoundException e) {
            source.sendError(TYPE_FILE_NOT_FOUND.apply(itemType));
            return 0;
        } catch (IOException e) {
            source.sendError(COULDNT_WRITE_TO_TYPE_FILE.apply(itemType));
            return 0;
        } catch (Exception e){
           source.sendError(PROB_COULDNT_READ_TYPE_FILE.apply(itemType));
           return 0;
        }

        WildToolAccessConfig.loadItemTypes(source.getPlayer().clientWorld.getRegistryManager());
        return 1;
    }

    private static int executeClearItemType(CommandContext<FabricClientCommandSource> context){
        String typeName = context.getArgument("itemType",String.class);
        WildToolAccessConfig.createEmptyItemType(typeName);
        WildToolAccessConfig.loadItemTypes(context.getSource().getPlayer().clientWorld.getRegistryManager());
        context.getSource().sendFeedback(Text.translatable("command.wildtoolaccess.stuff.cleared",typeName));
        return 1;
    }

    private static int executePrintItemType(CommandContext<FabricClientCommandSource> context){
        String typeName = context.getArgument("itemType",String.class);
        File typeFile = WildToolAccessConfig.ITEM_TYPE_DIRECTORY.toPath().resolve(typeName+".json").toFile();
        if(!typeFile.exists()){
            context.getSource().sendFeedback(Text.translatable("command.wildtoolaccess.stuff.no_stuff",typeName));
        }else{
            try {
                JsonObject obj = JsonHelper.deserialize(new FileReader(typeFile));
                JsonArray vals = JsonHelper.getArray(obj, "values");
                if(vals.isEmpty()){
                    context.getSource().sendFeedback(Text.translatable("command.wildtoolaccess.stuff.no_stuff",typeName));
                }

                vals.forEach(val->context.getSource().sendFeedback(Text.of(val.getAsString())));
            } catch (FileNotFoundException e) {
                context.getSource().sendError(TYPE_FILE_NOT_FOUND.apply(typeName));
                return 0;
            }
        }
        return 1;
    }

    private static ArrayList<Identifier> getItemListFromItemArgument(CommandContext<FabricClientCommandSource> context){
        ArrayList<Identifier> list = new ArrayList<Identifier>();
        list.add(Registries.ITEM.getId(context.getArgument("item",ItemStackArgument.class).getItem()));
        return list;
    }

    private static ArrayList<Identifier> getItemListFromAccessTypeArgument(CommandContext<FabricClientCommandSource> context, CommandRegistryAccess registryAccess){
        return new ArrayList<>(WildToolAccessConfig.getItemType(context.getArgument("type", String.class)).stream().map(Registries.ITEM::getId).toList());
    }

    private static ArrayList<Identifier> getItemListFromInventory(CommandContext<FabricClientCommandSource> context){
        ArrayList<Identifier> ids = new ArrayList<Identifier>();
        context.getSource().getClient().player.getInventory().main.forEach(stack-> {
            if(!stack.isEmpty()){
                ids.add(Registries.ITEM.getId(stack.getItem()));
            }
        } );
        return ids;
    }

    private enum Operation{
        ADD{
            @Override
            protected Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source, String typeName) {
                ArrayList<Identifier> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.already_contains", id.toString(), typeName));
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return Text.translatable("command.wildtoolaccess.stuff.added",addedIds,typeName);
            }
        },
        REMOVE{
            @Override
            protected Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source, String typeName) {
                ArrayList<Identifier> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.does_not_contain",id.toString(),typeName));
                        removedIds.remove(id);
                    }
                });

                return Text.translatable("command.wildtoolaccess.stuff.removed",removedIds,typeName);
            }
        };

        protected abstract Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source, String typeName);
    }

}
