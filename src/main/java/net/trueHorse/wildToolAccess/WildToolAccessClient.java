package net.trueHorse.wildToolAccess;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
    private static boolean bothWerePressed;

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
            InGameHudAccess hudAcc = ((InGameHudAccess)client.inGameHud);

            if(!WildToolAccessConfig.getBoolValue("toggleMode")){
                if(access1Binding.isPressed()&&access2Binding.isPressed()) {
                    bothWerePressed = true;
                    return;
                }

                if(access1Binding.isPressed()!=access1WasPressed) {
                    onAccessBindingHeldStatusChanged(access1Binding, hudAcc);
                }
                if(access2Binding.isPressed()!=access2WasPressed) {
                    onAccessBindingHeldStatusChanged(access2Binding, hudAcc);
                }

                access1WasPressed = access1Binding.isPressed();
                access2WasPressed = access2Binding.isPressed();
                bothWerePressed = false;
            }else{
                while (access1Binding.wasPressed()) {
                    onToggleBarBindingPressed(1, hudAcc);
                }
                while(access2Binding.wasPressed()){
                    onToggleBarBindingPressed(2, hudAcc);
                }
            }
        });
    }

    private void onAccessBindingHeldStatusChanged(KeyBinding accessBinding, InGameHudAccess hudAcc){
        if (accessBinding.isPressed()) {
            hudAcc.openAccessbar(1);
        } else {
            if(!bothWerePressed) {
                hudAcc.closeOpenAccessbar(true);
            }
        }
    }
    
    private void onToggleBarBindingPressed(int barNum, InGameHudAccess hudAcc){
        if(hudAcc.getOpenAccessBar()!=null){
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
