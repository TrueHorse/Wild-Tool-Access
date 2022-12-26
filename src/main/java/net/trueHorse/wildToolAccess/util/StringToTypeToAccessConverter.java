package net.trueHorse.wildToolAccess.util;

import net.minecraft.item.*;
import net.trueHorse.wildToolAccess.AccessType;
import net.trueHorse.wildToolAccess.StuffPlaceholder;

public class StringToTypeToAccessConverter {

    public static Class<?> convert(String string){
        return switch (Enum.valueOf(AccessType.class,string.toUpperCase())) {
            case TOOL -> ToolItem.class;
            case SWORD -> SwordItem.class;
            case RANGED_WEAPON -> RangedWeaponItem.class;
            case POTION -> PotionItem.class;
            case BUCKET -> BucketItem.class;
            case STUFF -> StuffPlaceholder.class;
        };
    }
}
