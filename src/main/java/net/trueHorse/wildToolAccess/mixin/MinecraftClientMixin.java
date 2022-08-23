package net.trueHorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.GameOptions;
import net.trueHorse.wildToolAccess.GameOptionsAccess;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public GameOptions options;
    @Shadow
    public InGameHud inGameHud;
    
    @Inject(method = "doAttack", at = @At(value = "HEAD"), cancellable = true)
    private void attackOrChoose(CallbackInfoReturnable<Boolean> info){
        if(((GameOptionsAccess)options).isAccessBarOpen()&&WildToolAccessConfig.getBoolValue("mouseSelect")){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(true);
            options.attackKey.setPressed(false);
            info.cancel();
        }
    }

    @Inject(method = "openPauseMenu", at = @At("HEAD"),cancellable = true)
    public void pauseMenuOrCloseAccess(boolean bl, CallbackInfo info){
        if(((GameOptionsAccess)options).isAccessBarOpen()&&WildToolAccessConfig.getBoolValue("escClose")){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(false);
            info.cancel();
        }
    }
}
