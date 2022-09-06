package net.trueHorse.wildToolAccess;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;

public class WildToolAccessSoundEvents {
    public static SoundEvent selectInAccess1;
    public static SoundEvent selectInAccess2;

    private static ArrayList<SoundEvent> soundEvents;

    public static SoundEvent register(Identifier id){
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void registerAll(){
        soundEvents.add(register(new Identifier("wildtoolaccess","select0")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select1")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select2")));
        soundEvents.add(register(new Identifier("wildtoolaccess","select3")));
    }

    public static void updateSoundEventsAsConfigured(){
        selectInAccess1 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound1"));
        selectInAccess1 = soundEvents.get(WildToolAccessConfig.getIntValue("selectSound2"));
    }
    
}
