package net.trueHooorse.wildToolAccess.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.GameOptions;
import net.trueHooorse.wildToolAccess.GameOptionsAccess;
import net.trueHooorse.wildToolAccess.InGameHudAccess;
import net.trueHooorse.wildToolAccess.config.WildToolAccessConfig;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public GameOptions options;
    @Shadow
    public InGameHud inGameHud;
    
    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doAttack()Z"))
    private boolean attackOrChoose(MinecraftClient client){
        if(((GameOptionsAccess)options).isAccessBarOpen()&&WildToolAccessConfig.getBoolValue("mouseSelect")){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(true);
            client.options.attackKey.setPressed(false);
            return false;
        }else{
            return ((MinecraftClientInvoker)client).invokeDoAttack();
        }
    }
}
