package net.trueHorse.wildToolAccess;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;

public class WildToolAccessSoundEvents {
    public static SoundEvent selectInAccess1;
    public static SoundEvent selectInAccess2;

    private static final ArrayList<SoundEvent> soundEvents = new ArrayList<SoundEvent>();

    public static SoundEvent register(ResourceLocation id){
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        ForgeRegistries.SOUND_EVENTS.register(id,event);
        return event;
    }

    public static void registerAll(){
        soundEvents.add(register(new ResourceLocation("wildtoolaccess","select0")));
        soundEvents.add(register(new ResourceLocation("wildtoolaccess","select1")));
        soundEvents.add(register(new ResourceLocation("wildtoolaccess","select2")));
        soundEvents.add(register(new ResourceLocation("wildtoolaccess","select3")));
    }

    public static void updateSoundEventsAsConfigured(){
        selectInAccess1 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound1"));
        selectInAccess2 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound2"));
    }
    
}
