package net.trueHorse.wildToolAccess;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class WildToolAccessClient implements ClientModInitializer{

    private static KeyBinding access1Binding;
    private static KeyBinding access2Binding;

    @Override
    public void onInitializeClient() {
        access1Binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.wildtoolaccess.access1",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.inventory"
        ));
        access2Binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.wildtoolaccess.access2",
            InputUtil.Type.KEYSYM,
            -1,
            "key.categories.inventory"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (access1Binding.wasPressed()) {
                onAccessBindingPressed(1, client);
            }
            while(access2Binding.wasPressed()){
                onAccessBindingPressed(2, client);
            }
        });
    }
    
    public void onAccessBindingPressed(int barNum, MinecraftClient client){
        InGameHudAccess hudAcc = ((InGameHudAccess)client.inGameHud);

        if(((GameOptionsAccess)client.options).isAccessBarOpen()){
            if(hudAcc.getOpenAccessBar().getNumber()==barNum){
                hudAcc.closeOpenAccessbar(true);
            }else{
                hudAcc.openAccessbar(barNum);
            }
        }else{
            hudAcc.openAccessbar(barNum);
        }
    }
}
