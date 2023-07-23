package net.trueHorse.wildToolAccess;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.trueHorse.wildToolAccess.commands.WildToolAccessCommand;
import net.trueHorse.wildToolAccess.commands.WildToolAccessCommands;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WildToolAccess.MODID)
public class WildToolAccess
{
    public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("wildtoolaccess");
    // Define mod id in a common place for everything to reference
    public static final String MODID = "wildtoolaccess";
    // Key mapping is lazily initialized so it doesn't exist until it is registered
    public static final Lazy<KeyMapping> ACCESS_1_BINDING = Lazy.of(() -> new KeyMapping(
            "key.wildtoolaccess.access1",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.inventory"
    ));
    public static final Lazy<KeyMapping> ACCESS_2_BINDING = Lazy.of(() -> new KeyMapping(
            "key.wildtoolaccess.access2",
            InputConstants.Type.KEYSYM,
            -1,
            "key.categories.inventory"
    ));

    public WildToolAccess()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Event is on the mod event bus only on the physical client
    @SubscribeEvent
    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(ACCESS_1_BINDING.get());
        event.register(ACCESS_2_BINDING.get());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        private static boolean access1WasPressed;
        private static boolean access2WasPressed;
        private static boolean bothWerePressed;
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            WildToolAccessConfig.loadCofigs();
            WildToolAccessConfig.loadStuffItems();
            WildToolAccessSoundEvents.registerAll();
            WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
                InGameHudAccess hudAcc = ((InGameHudAccess)Minecraft.getInstance().gui);

                if(!WildToolAccessConfig.getBoolValue("toggleMode")){
                    if(ACCESS_1_BINDING.get().isDown()&&ACCESS_2_BINDING.get().isDown()) {
                        bothWerePressed = true;
                        return;
                    }

                    if(ACCESS_1_BINDING.get().isDown()!=access1WasPressed) {
                        onAccessBindingHeldStatusChanged(ACCESS_1_BINDING, hudAcc);
                    }
                    if(ACCESS_2_BINDING.get().isDown()!=access2WasPressed) {
                        onAccessBindingHeldStatusChanged(ACCESS_2_BINDING, hudAcc);
                    }

                    access1WasPressed = ACCESS_1_BINDING.get().isDown();
                    access2WasPressed = ACCESS_2_BINDING.get().isDown();
                    bothWerePressed = false;
                }else {
                    while (ACCESS_1_BINDING.get().consumeClick()) {
                        onToggleBarBindingPressed(1, hudAcc);
                    }
                    while (ACCESS_2_BINDING.get().consumeClick()) {
                        onToggleBarBindingPressed(2, hudAcc);
                    }
                }
            }
        }
        
        private void onAccessBindingHeldStatusChanged(Lazy<KeyMapping> accessBinding, InGameHudAccess hudAcc){
            if (accessBinding.get().isDown()) {
                hudAcc.openAccessbar(accessBinding==ACCESS_1_BINDING?1:2);
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

    @Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientForgeEvent{

        @SubscribeEvent
        public static void onCommandsRegister(RegisterClientCommandsEvent event){
            WildToolAccessCommands.registerCommands(event.getDispatcher(),event.getBuildContext());
        }

    }
}
