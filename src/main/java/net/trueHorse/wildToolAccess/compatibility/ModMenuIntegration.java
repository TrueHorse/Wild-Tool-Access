package net.trueHorse.wildToolAccess.compatibility;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.trueHorse.wildToolAccess.duck.InGameHudAccess;
import net.trueHorse.wildToolAccess.WildToolAccessSoundEvents;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MinecraftClient client = MinecraftClient.getInstance();    
            ConfigBuilder confBuilder = ConfigBuilder.create()
                    .setParentScreen(client.currentScreen)
                    .setTitle(Text.translatable("screen.wildtoolaccess.config_screen"))
                    .setSavingRunnable(()->{
                        WildToolAccessConfig.createOrUpdateConfigFile();
                        WildToolAccessSoundEvents.updateSoundEventsAsConfigured();
                        ((InGameHudAccess)client.inGameHud).refreshAccessbars();
                    });
            ConfigCategory controlsCat = confBuilder.getOrCreateCategory(Text.translatable("config_category.wildtoolaccess.controls"));
            ConfigCategory functionalityCat = confBuilder.getOrCreateCategory(Text.translatable("config_category.wildtoolaccess.functionality"));
            ConfigCategory audioVisualCat = confBuilder.getOrCreateCategory(Text.translatable("config_category.wildtoolaccess.audioVisual"));

            ConfigEntryBuilder eb = confBuilder.entryBuilder();

            controlsCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.toggle_mode"), WildToolAccessConfig.getBoolValue("toggleMode"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.toggle_mode"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("toggleMode", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.left_click_select"), WildToolAccessConfig.getBoolValue("leftClickSelect"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.left_click_select"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("leftClickSelect", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.esc_close"), WildToolAccessConfig.getBoolValue("escClose"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.esc_close"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("escClose", Boolean.toString(newVal)))
                    .build());
            controlsCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.scroll_with_number_keys"), WildToolAccessConfig.getBoolValue("scrollWithNumberKeys"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.scroll_with_number_keys"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("scrollWithNumberKeys", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Text.translatable("option.wildtoolaccess.type_to_access_1"), WildToolAccessConfig.getStringValue("typeToAccess1"), string -> Text.translatable("option_val.wildtoolaccess."+string))
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.type_to_access_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("typeToAccess1", newVal))
                    .setSelections(WildToolAccessConfig.getItemTypes())
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Text.translatable("option.wildtoolaccess.type_to_access_2"), WildToolAccessConfig.getStringValue("typeToAccess2"), string -> Text.translatable("option_val.wildtoolaccess."+string))
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.type_to_access_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("typeToAccess2", newVal))
                    .setSelections(WildToolAccessConfig.getItemTypes())
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.leading_empty_slot"), WildToolAccessConfig.getBoolValue("leadingEmptySlot"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.leading_empty_slot"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("leadingEmptySlot", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.held_item_selected"), WildToolAccessConfig.getBoolValue("heldItemSelected"))
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.held_item_selected"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("heldItemSelected", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startStringDropdownMenu(Text.translatable("option.wildtoolaccess.item_info_shown"), WildToolAccessConfig.getStringValue("itemInfoShown"), string -> Text.translatable("option_val.wildtoolaccess."+string))
                    .setDefaultValue("enchantments")
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.item_info_shown"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("itemInfoShown", newVal))
                    .setSelections(List.of("all", "enchantments", "name", "none"))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.last_swapped_out_first"), WildToolAccessConfig.getBoolValue("lastSwappedOutFirst"))
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.last_swapped_out_first"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("lastSwappedOutFirst", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startBooleanToggle(Text.translatable("option.wildtoolaccess.put_to_the_right_if_possible"), WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.put_to_the_right_if_possible"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("putToTheRightIfPossible", Boolean.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.lock_swapping_to_slot"), WildToolAccessConfig.getIntValue("lockSwappingToSlot"),0, PlayerInventory.getHotbarSize())
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.lock_swapping_to_slot"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("lockSwappingToSlot", Integer.toString(newVal)))
                    .build());
            functionalityCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.hotbar_slot_after_swap"), WildToolAccessConfig.getIntValue("hotbarSlotAfterSwap"),0, PlayerInventory.getHotbarSize())
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.hotbar_slot_after_swap"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("hotbarSlotAfterSwap", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.select_sound_1"), WildToolAccessConfig.getIntValue("selectSound1"),0,3)
                    .setDefaultValue(1)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.select_sound_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("selectSound1", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.select_sound_2"), WildToolAccessConfig.getIntValue("selectSound2"),0,3)
                    .setDefaultValue(1)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.select_sound_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("selectSound2", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.bar_texture_1"), WildToolAccessConfig.getIntValue("barTexture1"),0,1)
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.bar_texture_1"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("barTexture1", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntSlider(Text.translatable("option.wildtoolaccess.bar_texture_2"), WildToolAccessConfig.getIntValue("barTexture2"),0,1)
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.bar_texture_2"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("barTexture2", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Text.translatable("option.wildtoolaccess.x_offset"), WildToolAccessConfig.getIntValue("xOffset"))
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.x_offset"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("xOffset", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Text.translatable("option.wildtoolaccess.y_offset"), WildToolAccessConfig.getIntValue("yOffset"))
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.y_offset"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("yOffset", Integer.toString(newVal)))
                    .build());
            audioVisualCat.addEntry(eb.startIntField(Text.translatable("option.wildtoolaccess.space_between_slots"), WildToolAccessConfig.getIntValue("spaceBetweenSlots"))
                    .setDefaultValue(0)
                    .setTooltip(Text.translatable("tooltip.wildtoolaccess.space_between_slots"))
                    .setSaveConsumer(newVal->WildToolAccessConfig.setValue("spaceBetweenSlots", Integer.toString(newVal)))
                    .build());

            return confBuilder.build();
        };
    }
}
