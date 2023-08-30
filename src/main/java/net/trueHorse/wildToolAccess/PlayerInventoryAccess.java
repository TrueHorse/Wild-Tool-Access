package net.trueHorse.wildToolAccess;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

import java.util.ArrayList;
import java.util.Collection;

public interface PlayerInventoryAccess {
    
    <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type);

    ArrayList<ItemStack> getAllMainStacksWithTag(Tag<Item> tag);

    ArrayList<ItemStack> getAllMainStacksOf(Collection<Item> items);
}
