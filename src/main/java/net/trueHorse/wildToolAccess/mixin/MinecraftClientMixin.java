package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.trueHorse.wildToolAccess.AccessBar;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Final
    @Shadow
    public Options options;
    @Final
    @Shadow
    public Gui inGameHud;
    
    @Inject(method = "handleInputEvents", at = @At(value = "HEAD"))
    private void handleAccessbarSelectInput(CallbackInfo info){
        if(((InGameHudAccess)inGameHud).getOpenAccessBar()!=null&&WildToolAccessConfig.getBoolValue("leftClickSelect")&&this.options.keyAttack.consumeClick()){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(true);
            ((KeyBindingAccess)options.keyAttack).setTimesPressed(0);
        }
    }

    @Inject(method = "openPauseMenu", at = @At("HEAD"),cancellable = true)
    public void pauseMenuOrCloseAccess(boolean bl, CallbackInfo info){
        if(((InGameHudAccess)inGameHud).getOpenAccessBar()!=null&&WildToolAccessConfig.getBoolValue("escClose")){
            ((InGameHudAccess)inGameHud).closeOpenAccessbar(false);
            info.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void closeBarOnScreenSwitch(Screen screen, CallbackInfo info){
        if(((InGameHudAccess)this.inGameHud).getOpenAccessBar()!=null){
            ((InGameHudAccess)this.inGameHud).closeOpenAccessbar(false);
        }
    }

    @Inject(method = "handleInputEvents",at = @At("HEAD"))
    private void handleAccessbarNumberKeySelection(CallbackInfo ci){
        AccessBar openAccessbar = ((InGameHudAccess)inGameHud).getOpenAccessBar();
        if(!WildToolAccessConfig.getBoolValue("scrollWithNumberKeys")||openAccessbar==null) return;

        for(int i = 0; i < 9; ++i) {
            if (options.keyHotbarSlots[i].consumeClick()) {
                openAccessbar.setSelectedAccessSlotIndex(Math.min(i,openAccessbar.getStacks().size()-1));
                ((KeyBindingAccess)options.keyHotbarSlots[i]).setTimesPressed(0);
            }
        }
    }
}
