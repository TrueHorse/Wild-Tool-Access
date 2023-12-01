package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.item.Item;
import net.trueHorse.wildToolAccess.ItemAccess;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements ItemAccess {

    public boolean isOfAccessType(Class<?> typeClass){
        if(typeClass.equals(StuffPlaceholder.class)){
            return WildToolAccessConfig.getStuffItems().contains(this);
        }else{
            return typeClass.isAssignableFrom(this.getClass());
        }
    }
}
