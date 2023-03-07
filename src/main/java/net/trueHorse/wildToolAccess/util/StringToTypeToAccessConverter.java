package net.trueHorse.wildToolAccess.util;

import net.minecraft.item.*;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;

public class StringToTypeToAccessConverter {

    public static Class<?> convert(String string) throws IllegalArgumentException{
            return switch (Enum.valueOf(AccessType.class,string.toUpperCase())) {
                case TOOLS -> ToolItem.class;
                case SWORDS -> SwordItem.class;
                case RANGED_WEAPONS -> RangedWeaponItem.class;
                case POTIONS -> PotionItem.class;
                case BUCKETS -> BucketItem.class;
                case STUFF -> StuffPlaceholder.class;
            };
    }
}
