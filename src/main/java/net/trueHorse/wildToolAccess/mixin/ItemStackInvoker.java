package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public interface ItemStackInvoker {

    @Invoker
    <T extends TooltipProvider> void invokeAddToTooltip(DataComponentType<T> componentType, Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type);

}
