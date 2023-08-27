package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindingAccess {

    @Accessor("timesPressed")
    void setTimesPressed(int timesPressed);
}
