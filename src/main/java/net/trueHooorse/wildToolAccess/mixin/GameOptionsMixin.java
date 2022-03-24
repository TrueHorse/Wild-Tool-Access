package net.trueHooorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.option.GameOptions;
import net.trueHooorse.wildToolAccess.GameOptionsAccess;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin implements GameOptionsAccess{
    public boolean accessBarOpen;

    @Override
    public boolean isAccessBarOpen() {
        return accessBarOpen;
    }

    @Override
    public void setAccessBarOpen(boolean accessBarOpen) {
        this.accessBarOpen = accessBarOpen;
    }
}
