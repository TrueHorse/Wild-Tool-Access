package net.trueHorse.wildToolAccess.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.trueHorse.wildToolAccess.WildToolAccess;
import net.trueHorse.wildToolAccess.util.StringToTypeToAccessConverter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class WildToolAccessConfig {

    private static final String[] OPTION_ORDER = {"leftClickSelect","escClose","selectSound1","selectSound2","barTexture1","barTexture2","xOffset","yOffset","spaceBetweenSlots","itemInfoShown","lastSwappedOutFirst","putToTheRightIfPossible","typeToAccess1","typeToAccess2"};
    private static Properties configs = new Properties();
    private static ImmutableSet<Item> stuffItems = ImmutableSet.copyOf(DefaultConfig.getDefaultStuffItems());
    public final static String MOD_CONFIG_DIR_NAME = FabricLoader.getInstance().getConfigDir() + "/wild_tool_access";
    public final static File MOD_CONFIG_FILE = new File(MOD_CONFIG_DIR_NAME+"/wild_tool_access.properties");
    public static final File STUFF_FILE = new File(MOD_CONFIG_DIR_NAME+"/stuff.json");

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

    public static void loadStuffItems(){
        ArrayList<Item> items = new ArrayList<Item>();

        if(STUFF_FILE.exists()){
            try {
                JsonArray vals = JsonHelper.getArray(JsonHelper.deserialize(new FileReader(STUFF_FILE)),"values");
                for(JsonElement element:vals){
                    if (element.isJsonPrimitive()) {
                        Optional<Item> item = Registries.ITEM.getOrEmpty(new Identifier(element.getAsString()));

                        if(item.isEmpty()){
                            WildToolAccess.LOGGER.error(element.getAsString()+" in stuff.json couldn't be added to stuff, because it isn't a registered item.");
                            continue;
                        }
                        items.add(item.get()) ;

                    } else {
                        WildToolAccess.LOGGER.error(element.getAsString()+" in stuff.json couldn't be added to stuff, because it is not json primitive.");
                    }
                }

                stuffItems = ImmutableSet.copyOf(items);
            } catch (FileNotFoundException e) {
                WildToolAccess.LOGGER.error("Stuff file was not found after existing. How?");
                e.printStackTrace();
            }
        }else{
            createOrUpdateFile(STUFF_FILE,DefaultConfig.defaultStuffJsonContent);
        }
    }

    public static void createOrUpdateConfigFile(){
        createOrUpdateFile(MOD_CONFIG_FILE,getConfigContentAsString(configs));
    }

    public static void createOrUpdateFile(File file, String content) {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if(file.exists()){
                boolean success = file.delete();
                if(!success) {
                    WildToolAccess.LOGGER.error(file.getName()+ " could not be deleted.");
                    WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
        }

        try {
            file.createNewFile();

            FileWriter confWriter = new FileWriter(file);
            confWriter.write(content);
            confWriter.close();
        } catch (IOException e) {
            WildToolAccess.LOGGER.error("Creation of "+file.getName()+" failed");
            e.printStackTrace();
        }
    }

    public static String getConfigContentAsString(Properties config){
        StringBuilder configString = new StringBuilder();
        for (String key : OPTION_ORDER) {
            configString.append(DefaultConfig.configComments.getProperty(key)).append('\n');
            configString.append(key).append("=").append(config.getProperty(key)).append('\n');
        }
        return configString.toString();
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
                WildToolAccess.LOGGER.error(key+" is set to "+configs.getProperty(key)+", which is not a numerical value.");
                return Integer.parseInt(DefaultConfig.defaultConfigs.getProperty(key));
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
        Class<?> val;
        try {
            val = StringToTypeToAccessConverter.convert(prop);
        }catch (IllegalArgumentException e){
            WildToolAccess.LOGGER.error("Configured access option "+prop+" for "+key+" does not exist.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            val = StringToTypeToAccessConverter.convert(DefaultConfig.defaultConfigs.getProperty(key));
        }
        return val;
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

    public static void setValue(String key, String val){
        if(configs.containsKey(key)){
            configs.replace(key,val);
        }else{
            WildToolAccess.LOGGER.error("Couldn't set config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    public static ImmutableSet<Item> getStuffItems() {
        return stuffItems;
    }
}
