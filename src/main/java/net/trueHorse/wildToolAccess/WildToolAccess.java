package net.trueHorse.wildToolAccess;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

    public static void onAccessBindingPressed(int barNum, Minecraft client){
        InGameHudAccess hudAcc = ((InGameHudAccess)client.gui);

        if(((InGameHudAccess)client.gui).getOpenAccessBar()!=null){
            if(hudAcc.isBarWithNumberOpen(barNum)){
                hudAcc.closeOpenAccessbar(true);
            }else{
                hudAcc.openAccessbar(barNum);
            }
        }else{
            hudAcc.openAccessbar(barNum);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            WildToolAccessConfig.loadCofigs();
            WildToolAccessConfig.loadStuffItems();
            WildToolAccessSoundEvents.registerAll();
            WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
            WildToolAccessCommands.registerCommands();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
                while (ACCESS_1_BINDING.get().consumeClick()) {
                    onAccessBindingPressed(1, Minecraft.getInstance());
                }
                while (ACCESS_2_BINDING.get().consumeClick()) {
                    onAccessBindingPressed(2, Minecraft.getInstance());
                }
            }
        }
    }
}
