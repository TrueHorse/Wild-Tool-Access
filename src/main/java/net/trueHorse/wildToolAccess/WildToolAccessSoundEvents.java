package net.trueHorse.wildToolAccess;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;

public class WildToolAccessSoundEvents {
    public static SoundEvent selectInAccess1;
    public static SoundEvent selectInAccess2;

    private static final ArrayList<SoundEvent> soundEvents = new ArrayList<SoundEvent>();

    public static SoundEvent register(Identifier id){
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerAll(){
        soundEvents.add(register(new Identifier("wildtoolaccess","select0")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select1")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select2")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select3")));
    }

    public static void updateSoundEventsAsConfigured(){
        selectInAccess1 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound1"));
        selectInAccess2 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound2"));
    }
    
}
