package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.Screen;
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
    public GameSettings options;
    @Final
    @Shadow
    public IngameGui gui;
    
    @Inject(method = "handleKeybinds", at = @At(value = "HEAD"))
    private void handleAccessbarSelectInput(CallbackInfo info){
        if(((InGameHudAccess) gui).getOpenAccessBar()!=null&&WildToolAccessConfig.leftClickSelect&&this.options.keyAttack.consumeClick()){
            ((InGameHudAccess) gui).closeOpenAccessbar(true);
            ((KeyBindingAccess)options.keyAttack).setClickCount(0);
        }
    }

    @Inject(method = "pauseGame", at = @At("HEAD"),cancellable = true)
    public void pauseMenuOrCloseAccess(boolean bl, CallbackInfo info){
        if(((InGameHudAccess) gui).getOpenAccessBar()!=null&&WildToolAccessConfig.escClose){
            ((InGameHudAccess) gui).closeOpenAccessbar(false);
            info.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void closeBarOnScreenSwitch(Screen screen, CallbackInfo info){
        if(((InGameHudAccess)this.gui).getOpenAccessBar()!=null){
            ((InGameHudAccess)this.gui).closeOpenAccessbar(false);
        }
    }

    @Inject(method = "handleKeybinds",at = @At("HEAD"))
    private void handleAccessbarNumberKeySelection(CallbackInfo ci){
        AccessBar openAccessbar = ((InGameHudAccess) gui).getOpenAccessBar();
        if(!WildToolAccessConfig.scrollWithNumberKeys||openAccessbar==null) return;

        for(int i = 0; i < 9; ++i) {
            if (options.keyHotbarSlots[i].consumeClick()) {
                openAccessbar.setSelectedAccessSlotIndex(Math.min(i,openAccessbar.getStacks().size()-1));
                ((KeyBindingAccess)options.keyHotbarSlots[i]).setClickCount(0);
            }
        }
    }
}
