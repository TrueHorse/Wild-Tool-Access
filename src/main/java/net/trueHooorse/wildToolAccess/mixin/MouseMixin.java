package net.trueHooorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import net.trueHooorse.wildToolAccess.GameOptionsAccess;
import net.trueHooorse.wildToolAccess.InGameHudAccess;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    double eventDeltaWheel;
    @Shadow
    MinecraftClient client;
    
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private void scrollInAccessBar(PlayerInventory inv, double e) {
        if(((GameOptionsAccess)client.options).isAccessBarOpen()){
            ((InGameHudAccess)client.inGameHud).getOpenAccessBar().scrollInAccessBar(e);
        }else{
            inv.scrollInHotbar(e);
        }
    }
}
