package net.trueHorse.wildToolAccess.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.wildToolAccess.WildToolAccess;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class StuffHandler {

    private static ImmutableSet<Item> stuffItems = ImmutableSet.copyOf(getDefaultStuffItems());
    public static final File STUFF_FILE = new File(WildToolAccessConfig.MOD_CONFIG_DIR_NAME+"/stuff.json");
    private static final String DEFAULT_STUFF_CONTENT = """
                    {
                        "values":[
                            "minecraft:torch",
                            "minecraft:ladder",
                            "minecraft:bucket",
                            "minecraft:cobblestone"
                        ]
                    }""";

    public static void loadStuffItems(){
        ArrayList<Item> items = new ArrayList<Item>();

        if(STUFF_FILE.exists()){
            try {
                JsonArray vals = GsonHelper.getAsJsonArray(GsonHelper.parse(new FileReader(STUFF_FILE)),"values");
                for(JsonElement element:vals){
                    if (element.isJsonPrimitive()) {

                        Optional<Holder<Item>> itemHolder = ForgeRegistries.ITEMS.getHolder(new ResourceLocation(element.getAsString()));

                        if(itemHolder.isEmpty()){
                            WildToolAccess.LOGGER.error(element.getAsString()+" in stuff.json couldn't be added to stuff, because it isn't a registered item.");
                            continue;
                        }
                        items.add(itemHolder.get().get()) ;

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
        JsonArray vals = GsonHelper.getAsJsonArray(GsonHelper.parse(DEFAULT_STUFF_CONTENT), "values");
        for (JsonElement element : vals) {
            if (element.isJsonPrimitive()) {
                items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString())));
            }
        }
        return items;
    }

    public static void createStuffFileWithValuesEmpty(){
        String content = """
                    {
                        "values":[
                        
                        ]
                    }""";
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
