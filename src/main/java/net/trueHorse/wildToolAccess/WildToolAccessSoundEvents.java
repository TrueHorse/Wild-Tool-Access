package net.trueHorse.wildToolAccess;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class WildToolAccessSoundEvents {
    public static SoundEvent SELECT_IN_ACCESS1;
    public static SoundEvent SELECT_IN_ACCESS2;

    public static SoundEvent register(Identifier id){
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void registerAll(){
        SELECT_IN_ACCESS1 = register(new Identifier("wildtoolaccess","select1"+WildToolAccessConfig.getIntValue("selectSound1")));
        SELECT_IN_ACCESS2 = register(new Identifier("wildtoolaccess","select2"+WildToolAccessConfig.getIntValue("selectSound2")));

    }
    
}
