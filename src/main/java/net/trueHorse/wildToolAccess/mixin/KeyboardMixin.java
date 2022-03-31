package net.trueHorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.trueHorse.wildToolAccess.GameOptionsAccess;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Shadow
    MinecraftClient client;
    
    @Redirect(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openPauseMenu(Z)V"))
    public void pauseMenuOrCloseAccess(MinecraftClient client, boolean bl){
        if(((GameOptionsAccess)client.options).isAccessBarOpen()&&WildToolAccessConfig.getBoolValue("escClose")){
            ((InGameHudAccess)client.inGameHud).closeOpenAccessbar(false);
        }else{
            client.openPauseMenu(bl);
        }
    }
}
