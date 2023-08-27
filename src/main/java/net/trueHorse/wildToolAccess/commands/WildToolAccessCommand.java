package net.trueHorse.wildToolAccess.commands;
/*
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgument;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class WildToolAccessCommand {

    private static final SimpleCommandExceptionType STUFF_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.file_not_found"));
    private static final SimpleCommandExceptionType COULDNT_WRITE_TO_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.couldnt_write"));
    private static final SimpleCommandExceptionType PROB_COULDNT_READ_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.probably_couldnt_read"));

    public static void register(CommandDispatcher<ClientCommandSourceStack> dispatcher, CommandBuildContext buildContext){
        dispatcher.register(Commands.literal("wta")
                .then(Commands.literal("stuff")
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType(buildContext)).suggests((context,builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context,buildContext),Operation.ADD, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemArgument.item(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType(buildContext)).suggests((context,builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context,buildContext),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.REMOVE, context.getSource())))
                                .then(Commands.literal("all").executes(context->WildToolAccessCommand.executeClearStuff(context.getSource()))))
                        .then(Commands.literal("list").executes(context -> WildToolAccessCommand.executePrintStuff(context.getSource())))
                        .then(Commands.literal("reset").executes(context -> WildToolAccessCommand.executeResetStuff(context.getSource())))
                ));
    }

    private static int executeModifyStuff(ArrayList<ResourceLocation> itemIds, Operation operation, ClientCommandSourceStack source) throws CommandSyntaxException {
        if(!WildToolAccessConfig.STUFF_FILE.exists()){
            WildToolAccessConfig.createStuffFileWithValuesEmpty();
        }

        JsonObject obj;
        try {
            obj = GsonHelper.parse(new FileReader(WildToolAccessConfig.STUFF_FILE));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        }

        JsonArray vals = GsonHelper.getAsJsonArray(obj,"values");
        Component feedback = operation.apply(itemIds,vals,source);

        try{
            FileWriter fwriter = new FileWriter(WildToolAccessConfig.STUFF_FILE);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendSuccess(()-> feedback,true);
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        } catch (IOException e) {
            throw COULDNT_WRITE_TO_STUFF.create();
        } catch (Exception e){
           throw PROB_COULDNT_READ_STUFF.create();
        }

        WildToolAccessConfig.loadStuffItems();
        return 1;
    }

    private static int executeClearStuff(ClientCommandSourceStack source){
        WildToolAccessConfig.createStuffFileWithValuesEmpty();
        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared"),true);
        return 1;
    }

    private static int executeResetStuff(ClientCommandSourceStack source){
        WildToolAccessConfig.resetStuffFile();
        source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.cleared"),true);
        return 1;
    }

    private static int executePrintStuff(ClientCommandSourceStack source) throws CommandSyntaxException {
        if(!WildToolAccessConfig.STUFF_FILE.exists()){
            source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff"),true);
        }else{
            try {
                JsonObject obj = GsonHelper.parse(new FileReader(WildToolAccessConfig.STUFF_FILE));
                JsonArray vals = GsonHelper.getAsJsonArray(obj, "values");
                if(vals.isEmpty()){
                    source.sendSuccess(()->Component.translatable("command.wildtoolaccess.stuff.no_stuff"),true);
                }

                vals.forEach(val->source.sendSuccess(()->Component.literal(val.getAsString()),true));
            } catch (FileNotFoundException e) {
                throw STUFF_FILE_NOT_FOUND.create();
            }
        }
        return 1;
    }

    private static ArrayList<ResourceLocation> getItemListFromItemArgument(CommandContext<ClientCommandSourceStack> context) throws CommandSyntaxException {
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>();
        list.add(ForgeRegistries.ITEMS.getKey(context.getArgument("item",ItemArgument.class).parse(new StringReader("")).getItem()));
        return list;
    }

    private static ArrayList<ResourceLocation> getItemListFromAccessTypeArgument(CommandContext<ClientCommandSourceStack> context, CommandBuildContext buildContext){
        ArrayList<ResourceLocation> itemIdsOfType = new ArrayList<ResourceLocation>();
        Class<?> type = context.getArgument("type", AccessTypeArgument.class).getType();

        if(type == StuffPlaceholder.class){
            itemIdsOfType.addAll(WildToolAccessConfig.getStuffItems().stream().map(ForgeRegistries.ITEMS::getKey).toList());
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

    private static ArrayList<ResourceLocation> getItemListFromInventory(CommandContext<ClientCommandSourceStack> context){
        ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
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
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, ClientCommandSourceStack source) {
                ArrayList<ResourceLocation> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendSuccess(Component.translatable("command.wildtoolaccess.stuff.already_contains", id.toString()));
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return Component.translatable("command.wildtoolaccess.stuff.added",addedIds);
            }
        },
        REMOVE{
            @Override
            protected Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, ClientCommandSourceStack source) {
                ArrayList<ResourceLocation> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendSuccess(Component.translatable("command.wildtoolaccess.stuff.does_not_contain",id.toString()));
                        removedIds.remove(id);
                    }
                });

                return Component.translatable("command.wildtoolaccess.stuff.removed",removedIds);
            }
        };

        protected abstract Component apply(ArrayList<ResourceLocation> ids, JsonArray vals, ClientCommandSourceStack source);
    }

}
*/