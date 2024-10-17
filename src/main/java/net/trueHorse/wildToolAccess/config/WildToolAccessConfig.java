package net.trueHorse.wildToolAccess.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.trueHorse.wildToolAccess.WildToolAccess;

import java.io.*;
import java.util.*;

public class WildToolAccessConfig {

    private static final String[] OPTION_ORDER = {"toggleMode", "leftClickSelect","escClose","scrollWithNumberKeys","selectSound1","selectSound2","barTexture1","barTexture2","xOffset","yOffset","spaceBetweenSlots","leadingEmptySlot","heldItemSelected","itemInfoShown","lastSwappedOutFirst","putToTheRightIfPossible","lockSwappingToSlot","hotbarSlotAfterSwap","typeToAccess1","typeToAccess2"};
    private static final Map<String,ConfigOption> configs = new HashMap<>();
    private final static Map<String,ImmutableSet<Item>> ITEM_TYPES = new HashMap<>();
    public final static String MOD_CONFIG_DIR_NAME = FabricLoader.getInstance().getConfigDir() + "/wild_tool_access";
    public final static File MOD_CONFIG_FILE = new File(MOD_CONFIG_DIR_NAME+"/wild_tool_access.properties");
    public static final File ITEM_TYPE_DIRECTORY = new File(MOD_CONFIG_DIR_NAME+"/item_types");
    private static final String DEFAULT_STUFF_CONTENT = """
                    {
                        "values":[
                            "minecraft:torch",
                            "minecraft:ladder",
                            "minecraft:bucket",
                            "minecraft:cobblestone"
                        ]
                    }""";
    private static final String DEFAULT_TOOLS_CONTENT = """
                    {
                        "values":[
                            "#minecraft:tools"
                        ]
                    }""";

    public static void loadCofigs(){
        resetConfigsToDefault();

        if(MOD_CONFIG_FILE.exists()){
            try {
                Properties tmpProperties = new Properties();
                FileReader reader = new FileReader(MOD_CONFIG_FILE);
                tmpProperties.load(reader);
                reader.close();
                tmpProperties.forEach((k,v)->{
                    if(configs.get(k)!=null) configs.get(k).setVal((String) v);
                });
                renameDeprecatedProperties();
            } catch (FileNotFoundException e) {
                WildToolAccess.LOGGER.error("Config file was not found after existing. How?");
                e.printStackTrace();
            } catch (IOException e) {
                WildToolAccess.LOGGER.error("Failed to read the actual config file.");
                e.printStackTrace();
            }
        }

        createOrUpdateConfigFile();
    }

    private static void resetConfigsToDefault(){
        configs.clear();
        configs.put("toggleMode",new ConfigOption("true",
                "If enabled, you don't need to hold down the key to keep the access bar open."));
        configs.put("leftClickSelect",new ConfigOption("true",
                "Left clicking will select current item."));
        configs.put("escClose",new ConfigOption("true",
                "Pressing esc will close the access bar without selecting an item."));
        configs.put("scrollWithNumberKeys",new ConfigOption("true",
                "You can use number keys to select items in access bars like you can in your hotbar."));
        configs.put("selectSound1",new ConfigOption("1",
                "the Sound you want to play, when selecting an item in bar 1 (0-3)"));
        configs.put("selectSound2",new ConfigOption("1",
                "see above, but for bar 2"));
        configs.put("barTexture1",new ConfigOption("0",
                "texture of the access bar 1  0->mine 1->my brothers (or use your own with a texture pack of cause)"));
        configs.put("barTexture2",new ConfigOption("0",
                "see above, but for bar 2"));
        configs.put("xOffset",new ConfigOption("0",
                "horizontal offset of the bar from the default position"));
        configs.put("yOffset",new ConfigOption("0",
                "vertical offset of the bar from the default position"));
        configs.put("spaceBetweenSlots",new ConfigOption("0",
                "space left between bar slots"));
        configs.put("leadingEmptySlot", new ConfigOption("true",
                "The first slot of the bars is empty."));
        configs.put("heldItemSelected",new ConfigOption("false",
                "When opening a bar your currently held item is selected, if it is contained in the bar."));
        configs.put("itemInfoShown",new ConfigOption("enchantments",
                "what information should be shown about the items  all->all; enchantments-> enchantments/potion effect and name;\n" +
                        "#name->name; non->non"));
        configs.put("lastSwappedOutFirst",new ConfigOption("true",
                "The tool swapped out last time should be shown in the first access bar slot next time."));
        configs.put("putToTheRightIfPossible",new ConfigOption("false",
                "The item that would be swapped out of your hotbar goes in the slot to the right instead, if that slot is empty"));
        configs.put("lockSwappingToSlot",new ConfigOption("0",
                "Locks swapping to that hotbar slot. Values <1 and >hotbar size disable this option."));
        configs.put("hotbarSlotAfterSwap",new ConfigOption("0",
                "After swapping your selected hotbar slot will be set to this slot. Values <1 and >hotbar size disable this option."));
        configs.put("typeToAccess1",new ConfigOption("tools",
                "what type of item you want to access  possible: tools, swords, ranged weapons, potions, buckets, stuff\n"+
                        "#Stuff is defined in the stuff.json file in the config folder and can be modified by hand or via in game command.\n"+
                        "#By default it includes torch, ladder, bucket and cobblestone."));
        configs.put("typeToAccess2",new ConfigOption("swords",
                "see above, but for access 2"));
    }

    public static void loadItemTypes(DynamicRegistryManager registries){
        if(ITEM_TYPE_DIRECTORY.exists()){
            for(File file : ITEM_TYPE_DIRECTORY.listFiles((file, name)->name.endsWith(".json"))) {
                ArrayList<Item> items = new ArrayList<Item>();
                try {
                    JsonArray vals = JsonHelper.getArray(JsonHelper.deserialize(new FileReader(file)),"values");

                    for(JsonElement element:vals){
                        if (element.isJsonPrimitive()) {
                            if(element.getAsString().startsWith("#")){
                                registries.get(RegistryKeys.ITEM).iterateEntries(TagKey.of(RegistryKeys.ITEM,new Identifier(element.getAsString().substring(1)))).forEach((entry)->items.add(new ItemStack(entry).getItem()));
                            }else{
                                Optional<Item> item = Registries.ITEM.getOrEmpty(new Identifier(element.getAsString()));

                                if(item.isEmpty()){
                                    WildToolAccess.LOGGER.error(element.getAsString()+" in "+file.getName()+" couldn't be added to stuff, because it isn't a registered item.");
                                }else{
                                    items.add(item.get()) ;
                                }
                            }
                        } else {
                            WildToolAccess.LOGGER.error(element.getAsString()+" in "+file.getName()+" couldn't be added to stuff, because it is not json primitive.");
                        }
                    }

                    ITEM_TYPES.put(file.getName().substring(0,file.getName().length()-4),ImmutableSet.copyOf(items));
                } catch (Exception e){
                    WildToolAccess.LOGGER.error(file.getName()+" could not be read.\n"+e.getMessage());
                }
            }
        }else{
            createDefaultItemTypes();
        }
    }

    public static void createOrUpdateConfigFile(){
        createOrUpdateFile(MOD_CONFIG_FILE,getConfigContentAsString());
    }

    public static void createEmptyItemType(String name){
        String content = """
                    {
                        "values":[
                        
                        ]
                    }""";
        createOrUpdateFile(ITEM_TYPE_DIRECTORY.toPath().resolve(name+".json").toFile(),content);
    }

    public static void createDefaultItemTypes(){
        createOrUpdateFile(ITEM_TYPE_DIRECTORY.toPath().resolve("stuff.json").toFile(),DEFAULT_STUFF_CONTENT);
        createOrUpdateFile(ITEM_TYPE_DIRECTORY.toPath().resolve("tools.json").toFile(),DEFAULT_TOOLS_CONTENT);
    }

    public static void createOrUpdateFile(File file, String content) {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        try {
            FileWriter confWriter = new FileWriter(file);
            confWriter.write(content);
            confWriter.close();
        } catch (IOException e) {
            WildToolAccess.LOGGER.error("Creation of "+file.getName()+" failed");
            e.printStackTrace();
        }
    }

    public static String getConfigContentAsString(){
        StringBuilder configString = new StringBuilder();
        ConfigOption option;
        for (String key : OPTION_ORDER) {
            option = configs.get(key);
            configString.append("#").append(option.getDescription()).append("\n");
            configString.append(key).append("=").append(option.getVal()).append("\n");
        }
        return configString.toString();
    }

    private static void renameDeprecatedProperties(){
        String[] deprecatedKeys = {"labels","mouseSelect","moveIfNextEmpty","access1","access2"};
        String[] replacements = {"itemInfoShown","leftClickSelect","putToTheRightIfPossible","typeToAccess1","typeToAccess2"};

        for(int i=0;i<deprecatedKeys.length;i++){
            if(configs.containsKey(deprecatedKeys[i])){
                configs.put(replacements[i],configs.get(deprecatedKeys[i]));
                configs.remove(deprecatedKeys[i]);
            }
        }
    }

    public static int getIntValue(String key){
        if(configs.containsKey(key)){
            try{
                return Integer.parseInt(configs.get(key).getVal());
            }catch(NumberFormatException e){
                e.printStackTrace();
                WildToolAccess.LOGGER.error(key+" is set to "+configs.get(key).getVal()+", which is not a numerical value.");
                return Integer.parseInt(configs.get(key).getDefaultVal());
            }
        }else {
            WildToolAccess.LOGGER.error("Couldn't get integer config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return -1;
        }
    }

    public static boolean getBoolValue(String key){
        if(configs.containsKey(key)){
            return Boolean.parseBoolean(configs.get(key).getVal());
        }else{
            WildToolAccess.LOGGER.error("Couldn't get boolean config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return false;
        }
    }

    public static String getStringValue(String key){
        if(configs.containsKey(key)){
            return configs.get(key).getVal().toLowerCase();
        }else {
            WildToolAccess.LOGGER.error("Couldn't get string config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
            return "";
        }
    }

    public static void setValue(String key, String val){
        if(configs.containsKey(key)){
            configs.get(key).setVal(val);
        }else{
            WildToolAccess.LOGGER.error("Couldn't set config option. Key "+key+" isn't present.");
            WildToolAccess.LOGGER.info(Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    public static ImmutableSet<Item> getItemType(String name) {
        return ITEM_TYPES.get(name);
    }

    public static Set<String> getItemTypes(){
        return ITEM_TYPES.keySet();
    }
}
