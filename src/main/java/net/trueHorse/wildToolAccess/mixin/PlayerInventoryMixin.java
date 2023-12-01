package net.trueHorse.wildToolAccess.mixin;

import java.util.ArrayList;
import java.util.Collection;

import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;
import net.trueHorse.wildToolAccess.PlayerInventoryAccess;
import net.trueHorse.wildToolAccess.InGameHudAccess;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements PlayerInventoryAccess{

    @Final
    @Shadow
    public DefaultedList<ItemStack> main;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void scrollInAccessBar(double scrollAmount, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(((InGameHudAccess)client.inGameHud).getOpenAccessBar()!=null){
            ((InGameHudAccess)client.inGameHud).getOpenAccessBar().scrollInAccessBar(scrollAmount);
            info.cancel();
        }
    }


    @Override
    public <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

        if(type.equals(StuffPlaceholder.class)){
            stacks.addAll(this.getAllMainStacksOf(WildToolAccessConfig.getStuffItems()));
        }else{
            for (ItemStack itemStack : main) {
                if (type.isAssignableFrom(itemStack.getItem().getClass())) {
                    stacks.add(itemStack);
                }
            }
        }

        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllMainStacksWithTag(TagKey<Item> tag){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : main) {
            if (itemStack.isIn(tag)) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllMainStacksOf(Collection<Item> items) {
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack itemStack : main) {
            if (items.contains(itemStack.getItem())) {
                stacks.add(itemStack);
            }
        }
        return stacks;
    }
}
