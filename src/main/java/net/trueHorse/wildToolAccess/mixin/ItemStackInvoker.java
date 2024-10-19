package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public interface ItemStackInvoker {

    @org.spongepowered.asm.mixin.gen.Invoker
    <T extends TooltipAppender> void invokeAppendTooltip(ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type);

}
