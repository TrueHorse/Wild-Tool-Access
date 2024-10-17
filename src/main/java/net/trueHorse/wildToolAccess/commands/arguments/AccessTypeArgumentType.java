package net.trueHorse.wildToolAccess.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class AccessTypeArgumentType implements ArgumentType<String>{

    private static final SimpleCommandExceptionType MISSING_TYPE = new SimpleCommandExceptionType(Text.translatable("argument.wildtoolaccess.type_to_access.missing"));
    private static final DynamicCommandExceptionType TYPE_UNKNOWN = new DynamicCommandExceptionType(input -> Text.translatable("argument.wildtoolaccess.type_to_access.unknown",input));
    private final RegistryWrapper<Item> registryWrapper;

    public AccessTypeArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.registryWrapper = commandRegistryAccess.createWrapper(RegistryKeys.ITEM);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        if(!reader.canRead()){
            throw MISSING_TYPE.create();
        }
        String input = reader.readString();

        if(!WildToolAccessConfig.getItemTypes().contains(input)){
            throw TYPE_UNKNOWN.createWithContext(reader,input);
        }else{
            return input;
        }
    }
}
