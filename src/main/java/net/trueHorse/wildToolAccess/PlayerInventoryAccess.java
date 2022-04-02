package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;

public interface PlayerInventoryAccess {
    
    public <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type);

    public ArrayList<ItemStack> getAllMainStacksWithTag(Tag<Item> tag);

    public void swapSlotWithSelected(int slot);

    public void moveSelectedAndSlot(int slot);

}
