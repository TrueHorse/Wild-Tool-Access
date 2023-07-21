package net.trueHorse.wildToolAccess;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.trueHorse.wildToolAccess.commands.WildToolAccessCommands;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.lwjgl.glfw.GLFW;

public class WildToolAccessClient implements ClientModInitializer{

    private static KeyBinding access1Binding;
    private static KeyBinding access2Binding;
    private static boolean access1WasPressed;
    private static boolean access2WasPressed;

    @Override
    public void onInitializeClient() {
        WildToolAccessConfig.loadCofigs();
        WildToolAccessConfig.loadStuffItems();
        WildToolAccessSoundEvents.registerAll();
        WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
        WildToolAccessCommands.registerCommands();

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
            if(!WildToolAccessConfig.getBoolValue("toggleMode")){
                boolean access1IsPressed = false;
                boolean access2IsPressed = false;

                while (access1Binding.wasPressed()) {
                    access1IsPressed = true;
                }
                while(access2Binding.wasPressed()){
                    access2IsPressed = true;
                }

                if(access1IsPressed!=access1WasPressed){
                    onAccessBindingPressed(1, client);
                }
                if(access2IsPressed!=access2WasPressed){
                    onAccessBindingPressed(2, client);
                }

                access1WasPressed = access1IsPressed;
                access2WasPressed = access2IsPressed;
            }else{
                while (access1Binding.wasPressed()) {
                    onAccessBindingPressed(1, client);
                }
                while(access2Binding.wasPressed()){
                    onAccessBindingPressed(2, client);
                }
            }
        });
    }
    
    public void onAccessBindingPressed(int barNum, MinecraftClient client){
        InGameHudAccess hudAcc = ((InGameHudAccess)client.inGameHud);

        if(((InGameHudAccess)client.inGameHud).getOpenAccessBar()!=null){
            if(hudAcc.isBarWithNumberOpen(barNum)){
                hudAcc.closeOpenAccessbar(true);
            }else{
                hudAcc.openAccessbar(barNum);
            }
        }else{
            hudAcc.openAccessbar(barNum);
        }
    }
}
