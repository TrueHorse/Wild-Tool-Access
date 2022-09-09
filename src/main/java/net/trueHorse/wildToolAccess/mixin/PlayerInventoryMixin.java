package net.trueHorse.wildToolAccess.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
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
    @Shadow
    public int selectedSlot;
    @Final
    @Shadow
    public PlayerEntity player;

    @Shadow
    public ItemStack getStack(int slot){return null;}
    @Shadow
    public void setStack(int slot, ItemStack stack){}
    @Shadow
    public ItemStack getMainHandStack(){return null;}

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
        for(int j=0;j<main.size();j++){
            if(type.isAssignableFrom(main.get(j).getItem().getClass())){
                stacks.add(main.get(j));
            }
        }
        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllMainStacksWithTag(TagKey<Item> tag){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for(int j=0;j<main.size();j++){
            if(main.get(j).isIn(tag)){
                stacks.add(main.get(j));
            }
        }
        return stacks;
    }

    @Override
    public void swapSlotWithSelected(int slot){
        ItemStack slotStack = getStack(slot).copy();
        setStack(slot, getMainHandStack());
        setStack(this.selectedSlot, slotStack);
        player.currentScreenHandler.sendContentUpdates();
    }
    @Override
    public void moveSelectedAndSlot(int slot) {
        if(this.selectedSlot!=8){
            setStack(this.selectedSlot+1, getStack(selectedSlot).copy());
            setStack(this.selectedSlot, ItemStack.EMPTY);
        }
        swapSlotWithSelected(slot);
    }
}
