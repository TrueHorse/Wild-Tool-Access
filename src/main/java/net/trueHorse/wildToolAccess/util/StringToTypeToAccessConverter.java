package net.trueHorse.wildToolAccess.util;

import net.minecraft.item.*;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;

public class StringToTypeToAccessConverter {

    public static Class<?> convert(String string) throws IllegalArgumentException{
        switch (Enum.valueOf(AccessType.class,string.toUpperCase())) {
            case TOOLS: return ToolItem.class;
            case SWORDS: return SwordItem.class;
            case RANGED_WEAPONS: return ShootableItem.class;
            case POTIONS: return PotionItem.class;
            case BUCKETS: return BucketItem.class;
            case STUFF: return StuffPlaceholder.class;
            default: throw new IllegalArgumentException();
        }
    }
}
