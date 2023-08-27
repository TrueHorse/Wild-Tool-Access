package net.trueHorse.wildToolAccess.util;

import net.minecraft.world.item.*;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;

public class StringToTypeToAccessConverter {

    public static Class<?> convert(String string) throws IllegalArgumentException{
            return switch (Enum.valueOf(AccessType.class,string.toUpperCase())) {
                case TOOLS -> TieredItem.class;
                case SWORDS -> SwordItem.class;
                case RANGED_WEAPONS -> ProjectileWeaponItem.class;
                case POTIONS -> PotionItem.class;
                case BUCKETS -> BucketItem.class;
                case STUFF -> StuffPlaceholder.class;
            };
    }
}
