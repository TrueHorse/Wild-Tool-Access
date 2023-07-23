package net.trueHorse.wildToolAccess.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgument;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.io.*;
import java.util.ArrayList;

public class WildToolAccessCommand {

    private static final SimpleCommandExceptionType STUFF_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.file_not_found"));
    private static final SimpleCommandExceptionType COULDNT_WRITE_TO_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.couldnt_write"));
    private static final SimpleCommandExceptionType PROB_COULDNT_READ_STUFF = new SimpleCommandExceptionType(Component.translatable("command.wildtoolaccess.stuff.probably_couldnt_read"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        dispatcher.register(Commands.literal("wta")
                .then(Commands.literal("stuff")
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemStackArgumentType.itemStack(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType(buildContext)).suggests((context,builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context,buildContext),Operation.ADD, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemStackArgumentType.itemStack(buildContext)).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
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

    private static int executeModifyStuff(ArrayList<Identifier> itemIds, Operation operation,FabricClientCommandSource source){
        if(!WildToolAccessConfig.STUFF_FILE.exists()){
            WildToolAccessConfig.createStuffFileWithValuesEmpty();
        }

        JsonObject obj;
        try {
            obj = JsonHelper.deserialize(new FileReader(WildToolAccessConfig.STUFF_FILE));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND;
        }

        JsonArray vals = JsonHelper.getArray(obj,"values");
        Text feedback = operation.apply(itemIds,vals,source);

        try{
            FileWriter fwriter = new FileWriter(WildToolAccessConfig.STUFF_FILE);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendFeedback(feedback);
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND;
        } catch (IOException e) {
            throw COULDNT_WRITE_TO_STUFF;
        } catch (Exception e){
           throw PROB_COULDNT_READ_STUFF;
        }

        WildToolAccessConfig.loadStuffItems();
        return 1;
    }

    private static int executeClearStuff(FabricClientCommandSource source){
        WildToolAccessConfig.createStuffFileWithValuesEmpty();
        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.cleared"));
        return 1;
    }

    private static int executeResetStuff(FabricClientCommandSource source){
        WildToolAccessConfig.resetStuffFile();
        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.cleared"));
        return 1;
    }

    private static int executePrintStuff(FabricClientCommandSource source){
        if(!WildToolAccessConfig.STUFF_FILE.exists()){
            source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.no_stuff"));
        }else{
            try {
                JsonObject obj = JsonHelper.deserialize(new FileReader(WildToolAccessConfig.STUFF_FILE));
                JsonArray vals = JsonHelper.getArray(obj, "values");
                if(vals.isEmpty()){
                    source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.no_stuff"));
                }

                vals.forEach(val->source.sendFeedback(Text.of(val.getAsString())));
            } catch (FileNotFoundException e) {
                throw STUFF_FILE_NOT_FOUND;
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
        ArrayList<Identifier> itemIdsOfType = new ArrayList<Identifier>();
        Class<?> type = context.getArgument("type", AccessTypeArgument.class).getType();

        if(type == StuffPlaceholder.class){
            itemIdsOfType.addAll(WildToolAccessConfig.getStuffItems().stream().map(Registries.ITEM::getId).toList());
        }else{
            List<Identifier> allItemIds = registryAccess.createWrapper(RegistryKeys.ITEM).streamKeys().map(RegistryKey::getValue).toList();
            for(Identifier id:allItemIds){
                Item item = Registries.ITEM.get(id);
                if(type.isAssignableFrom(item.getClass())){
                    itemIdsOfType.add(id);
                }
            }
        }

        return itemIdsOfType;
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
            protected Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source) {
                ArrayList<Identifier> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.already_contains", id.toString()));
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return Text.translatable("command.wildtoolaccess.stuff.added",addedIds);
            }
        },
        REMOVE{
            @Override
            protected Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source) {
                ArrayList<Identifier> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendFeedback(Text.translatable("command.wildtoolaccess.stuff.does_not_contain",id.toString()));
                        removedIds.remove(id);
                    }
                });

                return Text.translatable("command.wildtoolaccess.stuff.removed",removedIds);
            }
        };

        protected abstract Text apply(ArrayList<Identifier> ids, JsonArray vals, FabricClientCommandSource source);
    }

}
