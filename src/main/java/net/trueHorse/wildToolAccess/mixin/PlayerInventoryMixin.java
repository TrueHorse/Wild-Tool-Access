package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.PlayerInventoryAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(Inventory.class)
public class PlayerInventoryMixin implements PlayerInventoryAccess{

    @Final
    @Shadow
    public NonNullList<ItemStack> items;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void scrollInAccessBar(double scrollAmount, CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if(((InGameHudAccess)client.gui).getOpenAccessBar()!=null){
            ((InGameHudAccess)client.gui).getOpenAccessBar().scrollInAccessBar(scrollAmount);
            info.cancel();
        }
    }

    @Override
    public <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : items) {
            if (type.isAssignableFrom(itemStack.getItem().getClass())) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllMainStacksWithTag(TagKey<Item> tag){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : items) {
            if (itemStack.is(tag)) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllMainStacksOf(Collection<Item> items) {
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : this.items) {
            if (items.contains(itemStack.getItem())) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }
}
