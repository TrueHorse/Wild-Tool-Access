package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;

public interface PlayerInventoryAccess {
    
    <T> ArrayList<ItemStack> getAllMainStacksOfType(Class<T> type);

    ArrayList<ItemStack> getAllMainStacksWithTag(TagKey<Item> tag);

}
