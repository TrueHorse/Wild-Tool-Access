package net.trueHorse.wildToolAccess.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixinClient {

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void scrollInAccessBar(double scrollAmount, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(((InGameHudAccess)client.inGameHud).getOpenAccessBar()!=null){
            ((InGameHudAccess)client.inGameHud).getOpenAccessBar().scrollInAccessBar(scrollAmount);
            info.cancel();
        }
    }
}
