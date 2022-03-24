package net.trueHooorse.wildToolAccess.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.trueHooorse.wildToolAccess.StuffPlaceholder;

public class WildToolAccessConfig {

    private static Properties configs = new Properties();
    private final static String MOD_CONFIG_DIR_NAME = FabricLoader.getInstance().getConfigDir() + "/wild_tool_access";
    public static Tag<Item> stuffTag = TagRegistry.item(new Identifier("wildtoolaccess","stuff"));

    public static void loadCofigs(){
        configs = DefaultConfig.defaultConfigs;

        File confFile = new File(MOD_CONFIG_DIR_NAME+"/wild_tool_access.properties");
        if(confFile.exists()){
            try {
                configs.load(new FileReader(confFile));
            } catch (FileNotFoundException e) {
                System.out.println("Config file was not found after existing. How?");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Failed to read the actual config file.");
                e.printStackTrace();
            }
        }else{
            createConfigFromDefault(confFile);
        }
    }

    private static void createConfigFromDefault(File confFile) {
        if(!confFile.getParentFile().exists()){
            confFile.getParentFile().mkdirs();
        }
        try {
            confFile.createNewFile();

            FileWriter confWriter = new FileWriter(confFile);
            String defaultConfigContent = DefaultConfig.getConfigContentAsString();
            confWriter.write(defaultConfigContent);
            confWriter.close();
        } catch (IOException e) {
            System.out.println("Creation of config file failed");
            e.printStackTrace();
        }
    }

    public static int getIntValue(String key){
        try{
            return Integer.parseInt(configs.getProperty(key));
        }catch(NumberFormatException e){
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean getBoolValue(String key){
        return Boolean.parseBoolean(configs.getProperty(key));
    }

    public static Class<?> getClassValue(String key){
        String prop = configs.getProperty(key).toLowerCase();
        switch(prop){
            case "tools": return ToolItem.class;
            case "swords": return SwordItem.class;
            case "ranged weapons": return RangedWeaponItem.class;
            case "potions": return PotionItem.class;
            case "buckets": return BucketItem.class;
            case "stuff": return StuffPlaceholder.class;
            default:
            System.out.println("Configured access option does not exist.");
            return null;
        }
    }

    public static String getStringValue(String key){
        return configs.getProperty(key).toLowerCase();
    }
}
