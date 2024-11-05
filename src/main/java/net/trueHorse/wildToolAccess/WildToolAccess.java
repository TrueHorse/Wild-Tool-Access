package net.trueHorse.wildToolAccess;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.trueHorse.wildToolAccess.commands.WildToolAccessCommands;
import net.trueHorse.wildToolAccess.config.ItemTypeHandler;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
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

    public WildToolAccess(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register ourselves for server and other game events we are interested in
        //NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.CLIENT,WildToolAccessConfig.SPEC);
        WildToolAccessSoundEvents.registerAll(modEventBus);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
            ((InGameHudAccess)Minecraft.getInstance().gui).refreshAccessbars();
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, WildToolAccessConfig::getModConfigScreenFactory);
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(ACCESS_1_BINDING.get());
            event.register(ACCESS_2_BINDING.get());
        }
    }

    @EventBusSubscriber(modid = MODID,bus = EventBusSubscriber.Bus.GAME,value = Dist.CLIENT)
    public static class ClientForgeEvent{

        private static boolean access1WasPressed;
        private static boolean access2WasPressed;

        @SubscribeEvent
        public static void onCommandsRegister(RegisterClientCommandsEvent event){
            WildToolAccessCommands.registerCommands(event.getDispatcher(),event.getBuildContext());
        }

        @SubscribeEvent
        public static void onTagsLoaded(TagsUpdatedEvent event){
            ItemTypeHandler.loadItemTypes(event.getRegistryAccess());
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            InGameHudAccess hudAcc = ((InGameHudAccess)Minecraft.getInstance().gui);

            if(!WildToolAccessConfig.toggleMode){
                if(ACCESS_1_BINDING.get().isDown()&&ACCESS_2_BINDING.get().isDown()) {
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
            }else {
                while (ACCESS_1_BINDING.get().consumeClick()) {
                    onToggleBarBindingPressed(1, hudAcc);
                }
                while (ACCESS_2_BINDING.get().consumeClick()) {
                    onToggleBarBindingPressed(2, hudAcc);
                }
            }
        }

        private static void onAccessBindingHeldStatusChanged(Lazy<KeyMapping> accessBinding, InGameHudAccess hudAcc){
            if (accessBinding.get().isDown()) {
                hudAcc.openAccessbar(accessBinding==ACCESS_1_BINDING?1:2);
            } else {
                if(hudAcc.getOpenAccessBar()!=null) {
                    hudAcc.closeOpenAccessbar(true);
                }
            }
        }

        private static void onToggleBarBindingPressed(int barNum, InGameHudAccess hudAcc){
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
}
