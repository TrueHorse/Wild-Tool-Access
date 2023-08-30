package net.trueHorse.wildToolAccess.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.item.Item;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgument;
import net.trueHorse.wildToolAccess.commands.arguments.AccessTypeArgumentType;
import net.trueHorse.wildToolAccess.config.StuffHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class WildToolAccessCommand {

    private static final SimpleCommandExceptionType STUFF_FILE_NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("command.wildtoolaccess.stuff.file_not_found"));
    private static final SimpleCommandExceptionType COULDNT_WRITE_TO_STUFF = new SimpleCommandExceptionType(new TranslationTextComponent("command.wildtoolaccess.stuff.couldnt_write"));
    private static final SimpleCommandExceptionType PROB_COULDNT_READ_STUFF = new SimpleCommandExceptionType(new TranslationTextComponent("command.wildtoolaccess.stuff.probably_couldnt_read"));

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("wta")
                .then(Commands.literal("stuff")
                        .then(Commands.literal("add")
                                .then(Commands.argument("item", ItemArgument.item()).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.argument("type", new AccessTypeArgumentType()).suggests((context, builder) -> {
                                    for(AccessType enumType : AccessType.values()){
                                        builder.suggest(enumType.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                }).executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromAccessTypeArgument(context),Operation.ADD, context.getSource())))
                                .then(Commands.literal("inventory").executes(context->WildToolAccessCommand.executeModifyStuff(WildToolAccessCommand.getItemListFromInventory(context),Operation.ADD, context.getSource()))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("item", ItemArgument.item()).executes(context -> executeModifyStuff(WildToolAccessCommand.getItemListFromItemArgument(context),Operation.REMOVE, context.getSource())))
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

    private static int executeModifyStuff(ArrayList<ResourceLocation> itemIds, Operation operation, CommandSource source) throws CommandSyntaxException {
        if(!StuffHandler.STUFF_FILE.exists()){
            StuffHandler.createStuffFileWithValuesEmpty();
        }

        JsonObject obj;
        try {
            obj = JSONUtils.parse(new FileReader(StuffHandler.STUFF_FILE));
        } catch (FileNotFoundException e) {
            throw STUFF_FILE_NOT_FOUND.create();
        }

        JsonArray vals = JSONUtils.getAsJsonArray(obj,"values");
        ITextComponent feedback = operation.apply(itemIds,vals,source);

        try{
            FileWriter fwriter = new FileWriter(StuffHandler.STUFF_FILE);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(obj.toString());
            bwriter.close();
            fwriter.close();

            source.sendSuccess(feedback,false);
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

    private static int executeClearStuff(CommandSource source){
        StuffHandler.createStuffFileWithValuesEmpty();
        source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.cleared"),false);
        return 1;
    }

    private static int executeResetStuff(CommandSource source){
        StuffHandler.resetStuffFile();
        source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.cleared"),false);
        return 1;
    }

    private static int executePrintStuff(CommandSource source) throws CommandSyntaxException {
        if(!StuffHandler.STUFF_FILE.exists()){
            source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.no_stuff"),false);
        }else{
            try {
                JsonObject obj = JSONUtils.parse(new FileReader(StuffHandler.STUFF_FILE));
                JsonArray vals = JSONUtils.getAsJsonArray(obj, "values");
                if(vals.size()==0){
                    source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.no_stuff"),false);
                }

                vals.forEach(val->source.sendSuccess(ITextComponent.nullToEmpty(val.getAsString()),false));
            } catch (FileNotFoundException e) {
                throw STUFF_FILE_NOT_FOUND.create();
            }
        }
        return 1;
    }

    private static ArrayList<ResourceLocation> getItemListFromItemArgument(CommandContext<CommandSource> context) {
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>();
        list.add(ForgeRegistries.ITEMS.getKey(context.getArgument("item", ItemInput.class).getItem()));
        return list;
    }

    private static ArrayList<ResourceLocation> getItemListFromAccessTypeArgument(CommandContext<CommandSource> context){
        ArrayList<ResourceLocation> itemIdsOfType = new ArrayList<ResourceLocation>();
        Class<?> type = context.getArgument("type", AccessTypeArgument.class).getType();

        if(type == StuffPlaceholder.class){
            itemIdsOfType.addAll(StuffHandler.getStuffItems().stream().map(ForgeRegistries.ITEMS::getKey).collect(Collectors.toList()));
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

    private static ArrayList<ResourceLocation> getItemListFromInventory(CommandContext<CommandSource> context){
        ArrayList<ResourceLocation> ids = new ArrayList<ResourceLocation>();
        Minecraft.getInstance().player.inventory.items.forEach(stack-> {
            if(!stack.isEmpty()){
                ids.add(ForgeRegistries.ITEMS.getKey(stack.getItem()));
            }
        } );
        return ids;
    }

    private enum Operation{
        ADD{
            @Override
            protected ITextComponent apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSource source) {
                ArrayList<ResourceLocation> addedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if (vals.remove(new JsonPrimitive(id.toString()))) {
                        source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.already_contains", id.toString()),false);
                        addedIds.remove(id);
                    }
                    vals.add(new JsonPrimitive(id.toString()));
                });

                return new TranslationTextComponent("command.wildtoolaccess.stuff.added",addedIds);
            }
        },
        REMOVE{
            @Override
            protected ITextComponent apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSource source) {
                ArrayList<ResourceLocation> removedIds = new ArrayList<>(ids);

                ids.forEach(id->{
                    if(!vals.remove(new JsonPrimitive(id.toString()))){
                        source.sendSuccess(new TranslationTextComponent("command.wildtoolaccess.stuff.does_not_contain",id.toString()),false);
                        removedIds.remove(id);
                    }
                });

                return new TranslationTextComponent("command.wildtoolaccess.stuff.removed",removedIds);
            }
        };

        protected abstract ITextComponent apply(ArrayList<ResourceLocation> ids, JsonArray vals, CommandSource source);
    }

}
