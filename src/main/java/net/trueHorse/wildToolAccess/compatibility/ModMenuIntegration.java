package net.trueHorse.wildToolAccess.compatibility;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TranslatableText;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.WildToolAccessSoundEvents;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.Arrays;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ConfigBuilder confBuilder = ConfigBuilder.create()
                    .setParentScreen(client.currentScreen)
                    .setTitle(new TranslatableText("screen.wildtoolaccess.config_screen"))
                    .setSavingRunnable(()->{
                        WildToolAccessConfig.createOrUpdateConfigFile();
                        WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
                        ((InGameHudAccess)client.inGameHud).refreshAccessbars();
                    });
            ConfigCategory controlsCat = confBuilder.getOrCreateCategory(new TranslatableText("config_category.wildtoolaccess.controls"));
            ConfigCategory functionalityCat = confBuilder.getOrCreateCategory(new TranslatableText("config_category.wildtoolaccess.functionality"));
            ConfigCategory audioVisualCat = confBuilder.getOrCreateCategory(new TranslatableText("config_category.wildtoolaccess.audioVisual"));

            ConfigEntryBuilder eb = confBuilder.entryBuilder();

            controlsCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.toggle_mode"), WildToolAccessConfig.getBoolValue("toggleMode"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.toggle_mode"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("toggleMode", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.left_click_select"), WildToolAccessConfig.getBoolValue("leftClickSelect"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.left_click_select"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("leftClickSelect", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.esc_close"), WildToolAccessConfig.getBoolValue("escClose"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.esc_close"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("escClose", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.scroll_with_number_keys"), WildToolAccessConfig.getBoolValue("scrollWithNumberKeys"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.scroll_with_number_keys"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("scrollWithNumberKeys", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(new TranslatableText("option.wildtoolaccess.type_to_access_1"), WildToolAccessConfig.getStringValue("typeToAccess1"), string -> new TranslatableText("option_val.wildtoolaccess."+string))
                    .setDefaultValue("tools")
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.type_to_access_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("typeToAccess1", newVal))
                    .setSelections(Arrays.asList("tools","swords","ranged weapons","potions","buckets","stuff"))
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(new TranslatableText("option.wildtoolaccess.type_to_access_2"), WildToolAccessConfig.getStringValue("typeToAccess2"), string -> new TranslatableText("option_val.wildtoolaccess."+string))
                    .setDefaultValue("swords")
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.type_to_access_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("typeToAccess2", newVal))
                    .setSelections(Arrays.asList("tools","swords","ranged weapons","potions","buckets","stuff"))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.leading_empty_slot"), WildToolAccessConfig.getBoolValue("leadingEmptySlot"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.leading_empty_slot"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("leadingEmptySlot", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.held_item_selected"), WildToolAccessConfig.getBoolValue("heldItemSelected"))
                    .setDefaultValue(false)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.held_item_selected"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("heldItemSelected", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(new TranslatableText("option.wildtoolaccess.item_info_shown"), WildToolAccessConfig.getStringValue("itemInfoShown"), string -> new TranslatableText("option_val.wildtoolaccess."+string))
                    .setDefaultValue("enchantments")
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.item_info_shown"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("itemInfoShown", newVal))
                    .setSelections(Arrays.asList("all", "enchantments", "name", "none"))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.last_swapped_out_first"), WildToolAccessConfig.getBoolValue("lastSwappedOutFirst"))
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.last_swapped_out_first"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("lastSwappedOutFirst", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(new TranslatableText("option.wildtoolaccess.put_to_the_right_if_possible"), WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))
                    .setDefaultValue(false)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.put_to_the_right_if_possible"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("putToTheRightIfPossible", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.lock_swapping_to_slot"), WildToolAccessConfig.getIntValue("lockSwappingToSlot"),0, PlayerInventory.getHotbarSize())
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.lock_swapping_to_slot"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("lockSwappingToSlot", Integer.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.hotbar_slot_after_swap"), WildToolAccessConfig.getIntValue("hotbarSlotAfterSwap"),0, PlayerInventory.getHotbarSize())
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.hotbar_slot_after_swap"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("hotbarSlotAfterSwap", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.select_sound_1"), WildToolAccessConfig.getIntValue("selectSound1"),0,3)
                    .setDefaultValue(1)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.select_sound_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("selectSound1", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.select_sound_2"), WildToolAccessConfig.getIntValue("selectSound2"),0,3)
                    .setDefaultValue(1)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.select_sound_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("selectSound2", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.bar_texture_1"), WildToolAccessConfig.getIntValue("barTexture1"),0,1)
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.bar_texture_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("barTexture1", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(new TranslatableText("option.wildtoolaccess.bar_texture_2"), WildToolAccessConfig.getIntValue("barTexture2"),0,1)
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.bar_texture_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("barTexture2", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(new TranslatableText("option.wildtoolaccess.x_offset"), WildToolAccessConfig.getIntValue("xOffset"))
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.x_offset"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("xOffset", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(new TranslatableText("option.wildtoolaccess.y_offset"), WildToolAccessConfig.getIntValue("yOffset"))
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.y_offset"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("yOffset", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(new TranslatableText("option.wildtoolaccess.space_between_slots"), WildToolAccessConfig.getIntValue("spaceBetweenSlots"))
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("tooltip.wildtoolaccess.space_between_slots"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("spaceBetweenSlots", Integer.toString(newVal)))
                    .build());

            return confBuilder.build();
        };
    }
}
