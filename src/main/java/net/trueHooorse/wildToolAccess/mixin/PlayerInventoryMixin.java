package net.trueHooorse.wildToolAccess.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.collection.DefaultedList;
import net.trueHooorse.wildToolAccess.PlayerInventoryAccess;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements PlayerInventoryAccess{

    @Shadow
    private List<DefaultedList<ItemStack>> combinedInventory;
    @Shadow
    public int selectedSlot;
    @Final
    @Shadow
    public PlayerEntity player;

    @Shadow
    public ItemStack getStack(int slot){return null;};
    @Shadow
    public void setStack(int slot, ItemStack stack){};
    @Shadow
    public ItemStack getMainHandStack(){return null;};

    @Override
    public <T> ArrayList<ItemStack> getAllStacksOfType(Class<T> type){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for(int i=0;i<combinedInventory.size();i++){
            DefaultedList<ItemStack> inv = combinedInventory.get(i);
            for(int j=0;j<inv.size();j++){
                if(type.isAssignableFrom(inv.get(j).getItem().getClass())){
                    stacks.add(inv.get(j));
                }
            }
        }
        return stacks;
    }

    @Override
    public ArrayList<ItemStack> getAllStacksWithTag(Tag<Item> tag){
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        for(int i=0;i<combinedInventory.size();i++){
            DefaultedList<ItemStack> inv = combinedInventory.get(i);
            for(int j=0;j<inv.size();j++){
                if(tag.contains(inv.get(j).getItem())){
                    stacks.add(inv.get(j));
                }
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
