package net.trueHorse.wildToolAccess.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.trueHorse.wildToolAccess.StuffPlaceholder;
import net.trueHorse.wildToolAccess.WildToolAccess;

public class WildToolAccessConfig {

    private static final String[] OPTION_ORDER = {"leftClickSelect","escClose","selectSound1","selectSound2","barTexture1","barTexture2","xOffset","yOffset","spaceBetweenSlots","itemInfoShown","lastSwappedOutFirst","putToTheRightIfPossible","typeToAccess1","typeToAccess2"};
    private static Properties configs = new Properties();
    private final static String MOD_CONFIG_DIR_NAME = FabricLoader.getInstance().getConfigDir() + "/wild_tool_access";
    private final static File MOD_CONFIG_FILE = new File(MOD_CONFIG_DIR_NAME+"/wild_tool_access.properties");
    public static TagKey<Item> stuffTag = TagKey.of(Registry.ITEM_KEY, new Identifier("c","stuff"));

    public static void loadCofigs(){
        configs = DefaultConfig.defaultConfigs;

        if(MOD_CONFIG_FILE.exists()){
            try {
                configs.load(new FileReader(MOD_CONFIG_FILE));
                renameDeprecatedProperties();
            } catch (FileNotFoundException e) {
                WildToolAccess.LOGGER.error("Config file was not found after existing. How?");
                e.printStackTrace();
            } catch (IOException e) {
                WildToolAccess.LOGGER.error("Failed to read the actual config file.");
                e.printStackTrace();
            }
        }else{
            createOrUpdateConfigFile();
        }
    }

    public static void createOrUpdateConfigFile() {
        if(!MOD_CONFIG_FILE.getParentFile().exists()){
            MOD_CONFIG_FILE.getParentFile().mkdirs();
        }

        if(MOD_CONFIG_FILE.exists()){
            boolean success = MOD_CONFIG_FILE.delete();
            if(!success) {
                WildToolAccess.LOGGER.error("Config file could not be deleted.");
                WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }

        try {
            MOD_CONFIG_FILE.createNewFile();

            FileWriter confWriter = new FileWriter(MOD_CONFIG_FILE);
            confWriter.write(getConfigContentAsString(configs));
            confWriter.close();
        } catch (IOException e) {
            WildToolAccess.LOGGER.error("Creation of config file failed");
            e.printStackTrace();
        }
    }

    public static String getConfigContentAsString(Properties config){
        String configString = "";
        for (String key : OPTION_ORDER) {
            configString = configString+ DefaultConfig.configComments.getProperty(key)+'\n';
            configString = configString+key+"="+ config.getProperty(key)+'\n';
        }
        return configString;
    }

    private static void renameDeprecatedProperties(){
        String[] deprecatedKeys = {"labels","mouseSelect","moveIfNextEmpty","access1","access2"};
        String[] replacements = {"itemInfoShown","leftClickSelect","putToTheRightIfPossible","typeToAccess1","typeToAccess2"};

        for(int i=0;i<deprecatedKeys.length;i++){
            if(configs.containsKey(deprecatedKeys[i])){
                configs.put(replacements[i],configs.getProperty(deprecatedKeys[i]));
                configs.remove(deprecatedKeys[i]);
            }
        }
    }

    public static int getIntValue(String key){
        if(configs.containsKey(key)){
            try{
                return Integer.parseInt(configs.getProperty(key));
            }catch(NumberFormatException e){
                e.printStackTrace();
                return -1;
            }
        }else {
            WildToolAccess.LOGGER.error("Couldn't get integer config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return -1;
        }
    }

    public static boolean getBoolValue(String key){
        if(configs.containsKey(key)){
            return Boolean.parseBoolean(configs.getProperty(key));
        }else{
            WildToolAccess.LOGGER.error("Couldn't get boolean config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return false;
        }
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
                WildToolAccess.LOGGER.error("Configured access option for "+key+" does not exist.");
                WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
                return null;
        }
    }

    public static String getStringValue(String key){

        if(configs.containsKey(key)){
            return configs.getProperty(key).toLowerCase();
        }else {
            WildToolAccess.LOGGER.error("Couldn't get string config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return "";
        }
    }

    public static void setValue(String key, String val) {
        if (configs.containsKey(key)) {
            configs.replace(key, val);
        } else {
            WildToolAccess.LOGGER.error("Couldn't set config option. Key " + key + " isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }
}
