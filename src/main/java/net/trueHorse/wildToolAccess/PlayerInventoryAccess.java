package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

public interface PlayerInventoryAccess {
    
    <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type);

    ArrayList<ItemStack> getAllMainStacksWithTag(TagKey<Item> tag);

    void swapSlotWithSelected(int slot);

    void moveSelectedAndSlot(int slot);

}
