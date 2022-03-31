package net.trueHorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import net.trueHorse.wildToolAccess.GameOptionsAccess;
import net.trueHorse.wildToolAccess.InGameHudAccess;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    MinecraftClient client;
    
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private void scrollInAccessBar(PlayerInventory inv, double i) {
        if(((GameOptionsAccess)client.options).isAccessBarOpen()){
            ((InGameHudAccess)client.inGameHud).getOpenAccessBar().scrollInAccessBar(i);
        }else{
            inv.scrollInHotbar(i);
        }
    }
}
