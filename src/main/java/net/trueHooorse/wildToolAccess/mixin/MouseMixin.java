package net.trueHooorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.trueHooorse.wildToolAccess.GameOptionsAccess;
import net.trueHooorse.wildToolAccess.InGameHudAccess;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    double eventDeltaWheel;
    @Shadow
    MinecraftClient client;
    
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void scrollInAccessBar(long window, double horizontal, double vertical, CallbackInfo info, double d, float g) {
        if(((GameOptionsAccess)client.options).isAccessBarOpen()){
            ((InGameHudAccess)client.inGameHud).getOpenAccessBar().scrollInAccessBar(g);
            info.cancel();
        }
    }
}
