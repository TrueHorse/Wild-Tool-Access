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
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public GameOptions options;
    @Shadow
    public InGameHud inGameHud;
    
    @Inject(method = "handleInputEvents", at = @At(value = "HEAD"))
    private void handleAccessbarSelectInput(CallbackInfo info){
        if(((InGameHudAccess)inGameHud).getOpenAccessBar()!=null&&WildToolAccessConfig.getBoolValue("leftClickSelect")&&this.options.attackKey.wasPressed()){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(true);
        }
    }

    @Inject(method = "openPauseMenu", at = @At("HEAD"),cancellable = true)
    public void pauseMenuOrCloseAccess(boolean bl, CallbackInfo info){
        if(((InGameHudAccess)inGameHud).getOpenAccessBar()!=null&&WildToolAccessConfig.getBoolValue("escClose")){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(false);
            info.cancel();
        }
    }
}
