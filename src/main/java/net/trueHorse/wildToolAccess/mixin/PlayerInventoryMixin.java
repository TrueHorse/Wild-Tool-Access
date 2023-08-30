package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
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

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements PlayerInventoryAccess{

    @Final
    @Shadow
    public NonNullList<ItemStack> items;

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
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
    public ArrayList<ItemStack> getAllMainStacksWithTag(Tag<Item> tag){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : items) {
            if (tag.contains(itemStack.getItem())) {
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
