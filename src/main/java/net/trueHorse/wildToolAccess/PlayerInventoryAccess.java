package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

public interface PlayerInventoryAccess {
    
    public <T> ArrayList<ItemStack> getAllStacksOfType(Class<T> type);

    public ArrayList<ItemStack> getAllStacksWithTag(TagKey<Item> tag);

    public void swapSlotWithSelected(int slot);

    public void moveSelectedAndSlot(int slot);

}
