package net.trueHorse.wildToolAccess.config;

import java.util.Properties;

public class DefaultConfig {
    
    public static final Properties defaultConfigs = createDefaultConfigs();
    public static final Properties configComments = createConfigComments();

    private static Properties createDefaultConfigs(){
        Properties configs = new Properties();
        configs.setProperty("mouseSelect", "true");
        configs.setProperty("escClose", "true");
        configs.setProperty("selectSound1", "1");
        configs.setProperty("selectSound2", "1");
        configs.setProperty("barTexture1", "0");
        configs.setProperty("barTexture2", "0");
        configs.setProperty("xOffset", "0");
        configs.setProperty("yOffset", "0");
        configs.setProperty("spaceBetweenSlots", "0");
        configs.setProperty("labels", "enchantments");
        configs.setProperty("lastSwapedOutFirst", "true");
        configs.setProperty("moveIfNextEmpty", "false");
        configs.setProperty("access1", "tools");
        configs.setProperty("access2", "swords");
        return configs;
    }
    private static Properties createConfigComments(){
        Properties comments = new Properties();
        comments.setProperty("mouseSelect", "#This mods GitHib page: https://github.com/TrueHorse/Wild-Tool-Access \n"+
                                            "#if you want to be able to select an item in the access bar by leftclicking");
        comments.setProperty("escClose", "#if you want to be able to close the bar by pressing esc");
        comments.setProperty("selectSound1", "#the Sound you want to play, when selecting an item in bar 1");
        comments.setProperty("selectSound2", "#see above, but for bar 2");
        comments.setProperty("barTexture1", "#texture of the bar  0->mine 1->my brothers (or use your own with a texture pack of cause)");
        comments.setProperty("barTexture2", "#see above, but for bar 2");
        comments.setProperty("xOffset", "#horizontal offset of the bar from the default position");
        comments.setProperty("yOffset", "#vertical offset of the bar from the default position");
        comments.setProperty("spaceBetweenSlots", "#space left between bar slots");
        comments.setProperty("labels", "#what information should be shown about the items  all->all; enchantments-> enchantments/potion effect and name;\n"+
                                        "#name->name; non->non");
        comments.setProperty("lastSwapedOutFirst", "#if the tool swaped out should be shown in the first access bar slot next time");
        comments.setProperty("moveIfNextEmpty", "#if the item that would be swaped out of your hotbar should move to the right instead, if that slot is empty");
        comments.setProperty("access1", "#what type of item you want to access  possible: tools, swords, ranged weapons, potions, buckets, stuff\n"+
                                                "#Stuff is a custom item tag, so you can use a data pack to define, what you want it to be.\n"+
                                                "#By default it includes torch, ladder, bucket and cobblestone.");
        comments.setProperty("access2", "#see above, but for access 2");
        return comments;
    }
}
