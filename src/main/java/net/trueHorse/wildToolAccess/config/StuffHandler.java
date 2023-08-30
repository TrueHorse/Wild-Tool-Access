package net.trueHorse.wildToolAccess.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.WildToolAccess;

import java.io.*;
import java.util.ArrayList;

public class StuffHandler {

    private static ImmutableSet<Item> stuffItems = ImmutableSet.copyOf(getDefaultStuffItems());
    public static final File STUFF_FILE = new File(WildToolAccessConfig.MOD_CONFIG_DIR_NAME+"/stuff.json");
    private static final String DEFAULT_STUFF_CONTENT = "{\n"+
            "    \"values\":[\n"+
            "       \"minecraft:torch\",\n"+
            "       \"minecraft:ladder\",\n"+
            "       \"minecraft:bucket\",\n"+
            "       \"minecraft:cobblestone\"\n"+
            "    ]\n"+
            "}";

    public static void loadStuffItems(){
        ArrayList<Item> items = new ArrayList<Item>();

        if(STUFF_FILE.exists()){
            try {
                JsonArray vals = JSONUtils.getAsJsonArray(JSONUtils.parse(new FileReader(STUFF_FILE)),"values");
                for(JsonElement element:vals){
                    if (element.isJsonPrimitive()) {

                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString()));

                        items.add(item) ;

                    } else {
                        WildToolAccess.LOGGER.error(element.getAsString()+" in stuff.json couldn't be added to stuff, because it is not json primitive.");
                    }
                }

                stuffItems = ImmutableSet.copyOf(items);
            } catch (FileNotFoundException e) {
                WildToolAccess.LOGGER.error("Stuff file was not found after existing. How?");
                e.printStackTrace();
            } catch (Exception e){
                WildToolAccess.LOGGER.error("Stuff file could not be read as a .json file");
                e.printStackTrace();
            }
        }else{
            resetStuffFile();
        }
    }

    public static ArrayList<Item> getDefaultStuffItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        JsonArray vals = JSONUtils.getAsJsonArray(JSONUtils.parse(DEFAULT_STUFF_CONTENT), "values");
        for (JsonElement element : vals) {
            if (element.isJsonPrimitive()) {
                items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString())));
            }
        }
        return items;
    }

    public static void createStuffFileWithValuesEmpty(){
        String content = "{\n"+
                "    \"values\":[\n"+
                "\n"+
                "    ]\n"+
                "}";
        writeStuffFile(content);
    }

    public static void resetStuffFile(){
        writeStuffFile(DEFAULT_STUFF_CONTENT);
    }

    public static void writeStuffFile(String content){
        createOrUpdateFile(STUFF_FILE,content);
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

    public static ImmutableSet<Item> getStuffItems() {
        return stuffItems;
    }
}
