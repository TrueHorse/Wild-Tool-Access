package net.trueHorse.wildToolAccess.commands.arguments;
/*
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Component;
import net.minecraft.world.item.Item;
import net.trueHorse.wildToolAccess.util.StringToTypeToAccessConverter;

public class AccessTypeArgumentType implements ArgumentType<AccessTypeArgument>{

    private static final SimpleCommandExceptionType MISSING_TYPE = new SimpleCommandExceptionType(Component.translatable("arguments.wildtoolaccess.type_to_access.missing"));
    private static final DynamicCommandExceptionType TYPE_UNKNOWN = new DynamicCommandExceptionType(input -> Component.translatable("arguments.wildtoolaccess.type_to_access.unknown",input));
    private final RegistryWrapper<Item> registryWrapper;

    public AccessTypeArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.registryWrapper = commandRegistryAccess.createWrapper(RegistryKeys.ITEM);
    }

    @Override
    public AccessTypeArgument parse(StringReader reader) throws CommandSyntaxException {
        if(!reader.canRead()){
            throw MISSING_TYPE.create();
        }
        String input = reader.readString();

        Class<?> type;
        try {
            type = StringToTypeToAccessConverter.convert(input);
        }catch (IllegalArgumentException e){
            throw TYPE_UNKNOWN.createWithContext(reader,input);
        }
        return new AccessTypeArgument(type);
    }
}
*/