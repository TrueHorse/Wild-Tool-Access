package net.trueHorse.wildToolAccess;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;

public class WildToolAccessSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUNDS_REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,WildToolAccess.MODID);
    public static SoundEvent selectInAccess1;
    public static SoundEvent selectInAccess2;

    private static final ArrayList<RegistryObject<SoundEvent>> soundEventObjects = new ArrayList<>();

    public static RegistryObject<SoundEvent> register(ResourceLocation id){
        return SOUNDS_REGISTRY.register(id.getPath(),()->new SoundEvent(id));
    }

    public static void registerAll(){
        soundEventObjects.add(register(new ResourceLocation("wildtoolaccess","select0")));
        soundEventObjects.add(register(new ResourceLocation("wildtoolaccess","select1")));
        soundEventObjects.add(register(new ResourceLocation("wildtoolaccess","select2")));
        soundEventObjects.add(register(new ResourceLocation("wildtoolaccess","select3")));
        SOUNDS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void updateSoundEventsAsConfigured(){
        selectInAccess1 = soundEventObjects.get(WildToolAccessConfig.selectSound1).get();
        selectInAccess2 = soundEventObjects.get(WildToolAccessConfig.selectSound2).get();
    }
    
}
