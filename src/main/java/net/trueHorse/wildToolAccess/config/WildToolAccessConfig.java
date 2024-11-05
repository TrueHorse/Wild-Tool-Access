package net.trueHorse.wildToolAccess.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.WildToolAccess;
import net.trueHorse.wildToolAccess.WildToolAccessSoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = WildToolAccess.MODID, bus = EventBusSubscriber.Bus.MOD)
public class WildToolAccessConfig {
    public final static String MOD_CONFIG_DIR_NAME = Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/config/wild_tool_access";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue TOGGLE_MODE = BUILDER
            .comment("If enabled, you don't need to hold down the key to keep the access bar open.")
            .define("toggleMode", true);
    private static final ModConfigSpec.BooleanValue LEFT_CLICK_SELECT = BUILDER
            .comment("Left clicking will select current item.")
            .define("leftClickSelect", true);
    private static final ModConfigSpec.BooleanValue ESC_CLOSE = BUILDER
            .comment("Pressing esc will close the access bar without selecting an item.")
            .define("escClose",true);
    private static final ModConfigSpec.BooleanValue SCROLL_WITH_NUMBER_KEYS = BUILDER
            .comment("You can use number keys to select items in access bars like you can in your hotbar.")
            .define("scrollWithNumberKeys",true);
    private static final ModConfigSpec.IntValue SELECT_SOUND_1 = BUILDER
            .comment("the Sound you want to play, when selecting an item in bar 1 (0-3)")
            .defineInRange("selectSound1", 1, 0, 4);
    private static final ModConfigSpec.IntValue SELECT_SOUND_2 = BUILDER
            .comment("the Sound you want to play, when selecting an item in bar 2 (0-3)")
            .defineInRange("selectSound1", 1, 0, 4);
    private static final ModConfigSpec.IntValue BAR_TEXTURE_1 = BUILDER
            .comment("texture of the access bar 1  0->mine 1->my brothers (or use your own with a texture pack of cause)")
            .defineInRange("barTexture1", 0, 0, 1);
    private static final ModConfigSpec.IntValue BAR_TEXTURE_2 = BUILDER
            .comment("texture of the access bar 2  0->mine 1->my brothers (or use your own with a texture pack of cause)")
            .defineInRange("barTexture2", 0, 0, 1);
    private static final ModConfigSpec.IntValue X_OFFSET = BUILDER
            .comment("horizontal offset of the bar from the default position")
            .defineInRange("xOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue Y_OFFSET = BUILDER
            .comment("vertical offset of the bar from the default position")
            .defineInRange("yOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SPACE_BETWEEN_SLOTS = BUILDER
            .comment("space left between bar slots")
            .defineInRange("spaceBetweenSlots", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue LEADING_EMPTY_SLOT = BUILDER
            .comment("The first slot of the bars is empty.")
            .define("leadingEmptySlot",true);
    private static final ModConfigSpec.BooleanValue HELD_ITEM_SELECTED = BUILDER
            .comment("When opening a bar your currently held item is selected, if it is contained in the bar.")
            .define("heldItemSelected",false);
    private static final ModConfigSpec.ConfigValue<String> ITEM_INFO_SHOWN = BUILDER
            .comment("what information should be shown about the items  all->all; enchantments-> enchantments/potion effect and name;",
                    "name->name; non->non")
            .define("itemInfoShown", "enchantments",WildToolAccessConfig::validateItemInfoValue);
    private static final ModConfigSpec.BooleanValue LAST_SWAPPED_OUT_FIRST = BUILDER
            .comment("The tool swapped out last time should be shown in the first access bar slot next time.")
            .define("lastSwappedOutFirst",true);
    private static final ModConfigSpec.BooleanValue PUT_TO_THE_RIGHT_IF_POSSIBLE = BUILDER
            .comment("The item that would be swapped out of your hotbar goes in the slot to the right instead, if that slot is empty")
            .define("putToTheRightIfPossible",false);
    private static final ModConfigSpec.IntValue LOCK_SWAPPING_TO_SLOT = BUILDER
            .comment("Locks swapping to that hotbar slot. Values <1 and >hotbar size disable this option.")
            .defineInRange("lockSwappingToSlot", 0, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue HOTBAR_SLOT_AFTER_SWAP = BUILDER
            .comment("After swapping your selected hotbar slot will be set to this slot. Values <1 and >hotbar size disable this option.")
            .defineInRange("hotbarSlotAfterSwap", 0, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.ConfigValue<String> TYPE_TO_ACCESS_1 = BUILDER
            .comment("what type of item you want to access  possible: tools, swords, ranged weapons, potions, buckets, stuff",
                    "Stuff is defined in the stuff.json file in the config folder and can be modified by hand or via in game command.",
                    "By default it includes torch, ladder, bucket and cobblestone.")
            .define("typeToAccess1", "tools");
    private static final ModConfigSpec.ConfigValue<String> TYPE_TO_ACCESS_2 = BUILDER
            .comment("what type of item you want to access  possible: tools, swords, ranged weapons, potions, buckets, stuff",
                    "#Stuff is defined in the stuff.json file in the config folder and can be modified by hand or via in game command.",
                    "#By default it includes torch, ladder, bucket and cobblestone.")
            .define("typeToAccess2", "stuff");


    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean toggleMode;
    public static boolean leftClickSelect;
    public static boolean escClose;
    public static boolean scrollWithNumberKeys;
    public static int selectSound1;
    public static int selectSound2;
    public static int barTexture1;
    public static int barTexture2;
    public static int xOffset;
    public static int yOffset;
    public static int spaceBetweenSlots;
    public static boolean leadingEmptySlot;
    public static boolean heldItemSelected;
    public static String itemInfoShown;
    public static boolean lastSwappedOutFirst;
    public static boolean putToTheRightIfPossible;
    public static int lockSwappingToSlot;
    public static int hotbarSlotAfterSwap;
    public static String typeToAccess1;
    public static String typeToAccess2;

    private static boolean validateItemInfoValue(final Object obj){
        return obj instanceof final String itemName && List.of("all","enchantments","name","non").contains(itemName);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        loadConfigVals();
    }

    private static void loadConfigVals(){
        toggleMode = TOGGLE_MODE.get();
        leftClickSelect = LEFT_CLICK_SELECT.get();
        escClose = ESC_CLOSE.get();
        scrollWithNumberKeys = SCROLL_WITH_NUMBER_KEYS.get();
        selectSound1 = SELECT_SOUND_1.get();
        selectSound2 = SELECT_SOUND_2.get();
        barTexture1 = BAR_TEXTURE_1.get();
        barTexture2 = BAR_TEXTURE_2.get();
        xOffset = X_OFFSET.get();
        yOffset = Y_OFFSET.get();
        spaceBetweenSlots = SPACE_BETWEEN_SLOTS.get();
        leadingEmptySlot = LEADING_EMPTY_SLOT.get();
        heldItemSelected = HELD_ITEM_SELECTED.get();
        itemInfoShown = ITEM_INFO_SHOWN.get();
        lastSwappedOutFirst = LAST_SWAPPED_OUT_FIRST.get();
        putToTheRightIfPossible = PUT_TO_THE_RIGHT_IF_POSSIBLE.get();
        lockSwappingToSlot = LOCK_SWAPPING_TO_SLOT.get();
        hotbarSlotAfterSwap = HOTBAR_SLOT_AFTER_SWAP.get();
        typeToAccess1 = TYPE_TO_ACCESS_1.get();
        typeToAccess2 = TYPE_TO_ACCESS_2.get();
    }

    public static IConfigScreenFactory getModConfigScreenFactory() {
        return (modContainer,parent) -> {
            Minecraft client = Minecraft.getInstance();
            ConfigBuilder confBuilder = ConfigBuilder.create()
                    .setParentScreen(client.screen)
                    .setTitle(Component.translatable("screen.wildtoolaccess.config_screen"))
                    .setSavingRunnable(()->{
                        SPEC.save();
                        loadConfigVals();
                        WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
                        ((InGameHudAccess)client.gui).refreshAccessbars();
                    });
            ConfigCategory controlsCat = confBuilder.getOrCreateCategory(Component.translatable("config_category.wildtoolaccess.controls"));
            ConfigCategory functionalityCat = confBuilder.getOrCreateCategory(Component.translatable("config_category.wildtoolaccess.functionality"));
            ConfigCategory audioVisualCat = confBuilder.getOrCreateCategory(Component.translatable("config_category.wildtoolaccess.audioVisual"));

            ConfigEntryBuilder eb = confBuilder.entryBuilder();

            controlsCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.toggle_mode"), toggleMode)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.toggle_mode"))
                    .setSaveConsumer(WildToolAccessConfig.TOGGLE_MODE::set)
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.left_click_select"), leftClickSelect)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.left_click_select"))
                    .setSaveConsumer(LEFT_CLICK_SELECT::set)
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.esc_close"), escClose)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.esc_close"))
                    .setSaveConsumer(ESC_CLOSE::set)
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.scroll_with_number_keys"), scrollWithNumberKeys)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.scroll_with_number_keys"))
                    .setSaveConsumer(SCROLL_WITH_NUMBER_KEYS::set)
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Component.translatable("option.wildtoolaccess.type_to_access_1"), typeToAccess1, string -> (Objects.equals(Component.translatable("option_val.wildtoolaccess." + string).getString(), "option_val.wildtoolaccess." + string)) ? Component.literal(string) : Component.translatable("option_val.wildtoolaccess." + string))
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.type_to_access_1"))
                    .setSaveConsumer(TYPE_TO_ACCESS_1::set)
                    .setSelections(ItemTypeHandler.getItemTypes())
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Component.translatable("option.wildtoolaccess.type_to_access_2"), typeToAccess2, string -> Component.translatable("option_val.wildtoolaccess."+string))
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.type_to_access_2"))
                    .setSaveConsumer(TYPE_TO_ACCESS_2::set)
                    .setSelections(ItemTypeHandler.getItemTypes())
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.leading_empty_slot"), leadingEmptySlot)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.leading_empty_slot"))
                    .setSaveConsumer(LEADING_EMPTY_SLOT::set)
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.held_item_selected"), heldItemSelected)
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.held_item_selected"))
                    .setSaveConsumer(HELD_ITEM_SELECTED::set)
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Component.translatable("option.wildtoolaccess.item_info_shown"), itemInfoShown, string -> Component.translatable("option_val.wildtoolaccess."+string))
                    .setDefaultValue("enchantments")
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.item_info_shown"))
                    .setSaveConsumer(ITEM_INFO_SHOWN::set)
                    .setSelections(List.of("all", "enchantments", "name", "none"))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.last_swapped_out_first"), lastSwappedOutFirst)
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.last_swapped_out_first"))
                    .setSaveConsumer(LAST_SWAPPED_OUT_FIRST::set)
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Component.translatable("option.wildtoolaccess.put_to_the_right_if_possible"), putToTheRightIfPossible)
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.put_to_the_right_if_possible"))
                    .setSaveConsumer(PUT_TO_THE_RIGHT_IF_POSSIBLE::set)
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.lock_swapping_to_slot"), lockSwappingToSlot,0, Inventory.getSelectionSize())
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.lock_swapping_to_slot"))
                    .setSaveConsumer(LOCK_SWAPPING_TO_SLOT::set)
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.hotbar_slot_after_swap"), hotbarSlotAfterSwap,0, Inventory.getSelectionSize())
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.hotbar_slot_after_swap"))
                    .setSaveConsumer(HOTBAR_SLOT_AFTER_SWAP::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.select_sound_1"), selectSound1,0,3)
                    .setDefaultValue(1)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.select_sound_1"))
                    .setSaveConsumer(SELECT_SOUND_1::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.select_sound_2"), selectSound2,0,3)
                    .setDefaultValue(1)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.select_sound_2"))
                    .setSaveConsumer(SELECT_SOUND_2::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.bar_texture_1"), barTexture1,0,1)
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.bar_texture_1"))
                    .setSaveConsumer(BAR_TEXTURE_1::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Component.translatable("option.wildtoolaccess.bar_texture_2"), barTexture2,0,1)
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.bar_texture_2"))
                    .setSaveConsumer(BAR_TEXTURE_2::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Component.translatable("option.wildtoolaccess.x_offset"), xOffset)
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.x_offset"))
                    .setSaveConsumer(X_OFFSET::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Component.translatable("option.wildtoolaccess.y_offset"), yOffset)
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.y_offset"))
                    .setSaveConsumer(Y_OFFSET::set)
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Component.translatable("option.wildtoolaccess.space_between_slots"), spaceBetweenSlots)
                    .setDefaultValue(0)
                    .setTooltip(Component.translatable("tooltip.wildtoolaccess.space_between_slots"))
                    .setSaveConsumer(SPACE_BETWEEN_SLOTS::set)
                    .build());

            return confBuilder.build();
        };
    }
}
