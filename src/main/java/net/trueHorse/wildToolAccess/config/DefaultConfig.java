package net.trueHorse.wildToolAccess.config;

import java.util.Properties;

public class DefaultConfig {
    
    public static final Properties defaultConfigs = createDefaultConfigs();
    public static final Properties configComments = createConfigComments();
    public static final String defaultStuffJsonContent =
                    """
                    {
                        "values":[
                            "minecraft:torch",
                            "minecraft:ladder",
                            "minecraft:bucket",
                            "minecraft:cobblestone"
                        ]
                    }""";

    private static Properties createDefaultConfigs(){
        Properties configs = new Properties();
        configs.setProperty("leftClickSelect", "true");
        configs.setProperty("escClose", "true");
        configs.setProperty("selectSound1", "1");
        configs.setProperty("selectSound2", "1");
        configs.setProperty("barTexture1", "0");
        configs.setProperty("barTexture2", "0");
        configs.setProperty("xOffset", "0");
        configs.setProperty("yOffset", "0");
        configs.setProperty("spaceBetweenSlots", "0");
        configs.setProperty("itemInfoShown", "enchantments");
        configs.setProperty("lastSwappedOutFirst", "true");
        configs.setProperty("putToTheRightIfPossible", "false");
        configs.setProperty("typeToAccess1", "tools");
        configs.setProperty("typeToAccess2", "swords");
        return configs;
    }
    private static Properties createConfigComments(){
        Properties comments = new Properties();
        comments.setProperty("leftClickSelect", "#This mods GitHib page: https://github.com/TrueHorse/Wild-Tool-Access \n"+
                                            "#Left clicking will select current item.");
        comments.setProperty("escClose", "#Pressing esc will close the access bar without selecting an item.");
        comments.setProperty("selectSound1", "#the Sound you want to play, when selecting an item in bar 1 (0-3)");
        comments.setProperty("selectSound2", "#see above, but for bar 2");
        comments.setProperty("barTexture1", "#texture of the access bar 1  0->mine 1->my brothers (or use your own with a texture pack of cause)");
        comments.setProperty("barTexture2", "#see above, but for bar 2");
        comments.setProperty("xOffset", "#horizontal offset of the bar from the default position");
        comments.setProperty("yOffset", "#vertical offset of the bar from the default position");
        comments.setProperty("spaceBetweenSlots", "#space left between bar slots");
        comments.setProperty("itemInfoShown", "#what information should be shown about the items  all->all; enchantments-> enchantments/potion effect and name;\n"+
                                        "#name->name; non->non");
        comments.setProperty("lastSwappedOutFirst", "#The tool swapped out last time should be shown in the first access bar slot next time.");
        comments.setProperty("putToTheRightIfPossible", "#The item that would be swapped out of your hotbar goes in the slot to the right instead, if that slot is empty");
        comments.setProperty("typeToAccess1", "#what type of item you want to access  possible: tools, swords, ranged weapons, potions, buckets, stuff\n"+
                                                "#Stuff is a custom item tag, so you can use a data pack to define, what you want it to be.\n"+
                                                "#By default it includes torch, ladder, bucket and cobblestone.");
        comments.setProperty("typeToAccess2", "#see above, but for access 2");
        return comments;
    }
}
