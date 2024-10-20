package net.trueHorse.wildToolAccess;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;

public class WildToolAccessSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUNDS_REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT,WildToolAccess.MODID);
    public static SoundEvent selectInAccess1;
    public static SoundEvent selectInAccess2;

    private static final ArrayList<DeferredHolder<SoundEvent, SoundEvent>> soundEventObjects = new ArrayList<>();

    public static DeferredHolder<SoundEvent, SoundEvent> register(ResourceLocation id){
        return SOUNDS_REGISTRY.register(id.getPath(),()->SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerAll(IEventBus modEventBus){
        soundEventObjects.add(register(ResourceLocation.tryBuild("wildtoolaccess","select0")));
        soundEventObjects.add(register(ResourceLocation.tryBuild("wildtoolaccess","select1")));
        soundEventObjects.add(register(ResourceLocation.tryBuild("wildtoolaccess","select2")));
        soundEventObjects.add(register(ResourceLocation.tryBuild("wildtoolaccess","select3")));
        SOUNDS_REGISTRY.register(modEventBus);
    }

    public static void updateSoundEventsAsConfigured(){
        selectInAccess1 = soundEventObjects.get(WildToolAccessConfig.selectSound1).get();
        selectInAccess2 = soundEventObjects.get(WildToolAccessConfig.selectSound2).get();
    }
    
}
