package net.trueHorse.wildToolAccess.util;

import net.minecraft.item.*;
import net.trueHorse.wildToolAccess.StuffPlaceholder;

public class StringToTypeToAccessConverter {

    public static Class<?> convert(String string){
        return switch (string) {
            case "tools" -> ToolItem.class;
            case "swords" -> SwordItem.class;
            case "ranged weapons" -> RangedWeaponItem.class;
            case "potions" -> PotionItem.class;
            case "buckets" -> BucketItem.class;
            case "stuff" -> StuffPlaceholder.class;
            default -> null;
        };
    }
}
