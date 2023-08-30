package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccess {

    @Accessor("clickCount")
    void setClickCount(int timesPressed);
}
