package net.trueHorse.wildToolAccess.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.trueHorse.wildToolAccess.WildToolAccess;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ItemTypeHandler {

    private final static Map<String,ImmutableSet<Item>> ITEM_TYPES = new HashMap<>();
    public static final File ITEM_TYPE_DIRECTORY = new File(WildToolAccessConfig.MOD_CONFIG_DIR_NAME+"/item_types");
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

    public static void loadItemTypes(RegistryAccess registries){
        if(!ITEM_TYPE_DIRECTORY.exists()){
            createDefaultItemTypes();
        }

        for(File file : ITEM_TYPE_DIRECTORY.listFiles((file, name)->name.endsWith(".json"))) {
            ArrayList<Item> items = new ArrayList<Item>();
            try {
                JsonArray vals = GsonHelper.getAsJsonArray(GsonHelper.parse(new FileReader(file)),"values");
                for(JsonElement element:vals){
                    if (element.isJsonPrimitive()) {
                        if(element.getAsString().startsWith("#")){
                            registries.registryOrThrow(Registries.ITEM).getTagOrEmpty(TagKey.create(Registries.ITEM,new ResourceLocation(element.getAsString().substring(1)))).forEach((entry)->items.add(new ItemStack(entry).getItem()));
                        }else{
                            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(element.getAsString()));

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

                ITEM_TYPES.put(file.getName().substring(0,file.getName().length()-5),ImmutableSet.copyOf(items));

            } catch (Exception e){
                WildToolAccess.LOGGER.error(file.getName()+" could not be read.\n"+e.getMessage());
            }
        }
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

    public static ImmutableSet<Item> getItemType(String name) {
        return Objects.requireNonNullElseGet(ITEM_TYPES.get(name), ImmutableSet::of);
    }

    public static Set<String> getItemTypes(){
        return ITEM_TYPES.keySet();
    }
}
