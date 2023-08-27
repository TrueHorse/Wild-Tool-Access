package net.trueHorse.wildToolAccess.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.trueHorse.wildToolAccess.AccessBar;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.WildToolAccessSoundEvents;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public class InGameHudMixin implements InGameHudAccess{

    @Final @Shadow
    private Minecraft client;
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;
    private final ResourceLocation[] accessBarTextureSheets = {
        new ResourceLocation("wildtoolaccess","textures/gui/access_widgets0.png"),
        new ResourceLocation("wildtoolaccess","textures/gui/access_widgets1.png")};
    private AccessBar[] accessBars;
    private AccessBar openAccessbar;

    @Shadow
    private Player getCameraPlayer(){return null;}
    @Shadow
    private void renderHotbarItem(GuiGraphics context, int x, int y, float tickDelta, Player player, ItemStack stack, int seed){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(Minecraft client, ItemRenderer itemRenderer, CallbackInfo ci){
        accessBars = getAccessBarArray();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V",shift = At.Shift.AFTER))
    public void renderAccessBar(GuiGraphics context, float tickDelta, CallbackInfo ci){
        if(openAccessbar!=null){
            Player playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                ResourceLocation barTextures;
                barTextures = openAccessbar.getTextures();

                int firstSlotXCoordinate = scaledWidth / 2 -10+WildToolAccessConfig.getIntValue("xOffset");
                int yCoordinate = scaledHeight/2 -54+WildToolAccessConfig.getIntValue("yOffset");
                context.pose().pushPose();
                context.pose().translate(0.0F, 0.0F, -90.0F);

                int xCoordinate;
                int spaceBetweenSlots = 20+WildToolAccessConfig.getIntValue("spaceBetweenSlots");
                
                if(openAccessbar.getStacks().size()==1){
                    context.blit(barTextures, firstSlotXCoordinate, yCoordinate, 66, 0, 22, 22);
                }else{
                    int k;
                    for(k = 1; k < openAccessbar.getStacks().size()-1; ++k) {
                        xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                        context.blit(barTextures, xCoordinate, yCoordinate, 0, 0, 22, 22);
                    }
                    xCoordinate = firstSlotXCoordinate - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    context.blit(barTextures, xCoordinate, yCoordinate, 22, 0, 22, 22);
                    xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    context.blit(barTextures, xCoordinate, yCoordinate, 44, 0, 22, 22);
                }
                context.blit(barTextures, firstSlotXCoordinate - 1, yCoordinate - 1, 0, 22, 24, 22);

                context.pose().popPose();

                int seed =1;
                for(int i = 0; i < openAccessbar.getStacks().size(); ++i) {
                    xCoordinate = firstSlotXCoordinate + i * spaceBetweenSlots + 3 - spaceBetweenSlots*(openAccessbar.getSelectedAccessSlotIndex());
                    this.renderHotbarItem(context, xCoordinate, yCoordinate+3, tickDelta, playerEntity, openAccessbar.getStacks().get(i),seed++);
                }

                String labConf = WildToolAccessConfig.getStringValue("itemInfoShown");
                if(!labConf.equals("non")&&openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex())!=ItemStack.EMPTY){
                    renderLabels(context, labConf, firstSlotXCoordinate, yCoordinate);
                }
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(GuiGraphics context,String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex());
        List<Component> tooltip;
        if(labConf.equals("all")){
            tooltip = selectedStack.getTooltipLines(client.player, this.client.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            tooltip.remove(CommonComponents.EMPTY);
            tooltip.remove((Component.translatable("item.modifiers.mainhand")).withStyle(ChatFormatting.GRAY));
        }else{
            tooltip = new ArrayList<Component>();
            if(labConf.equals("name")||labConf.equals("enchantments")){
                MutableComponent name = (Component.literal("")).append(selectedStack.getDisplayName()).withStyle(selectedStack.getRarity().getStyleModifier());
                if (selectedStack.hasCustomHoverName()) {
                    name.withStyle(ChatFormatting.ITALIC);
                }
                tooltip.add(name);
            }
    
            if(labConf.equals("enchantments")){
                if (selectedStack.hasTag()) {
                       ItemStack.appendEnchantmentNames(tooltip, selectedStack.getEnchantmentTags());
                }
                if (selectedStack.getItem() instanceof PotionItem){
                    List<Component> temp = new ArrayList<Component>();
                    selectedStack.getItem().appendHoverText(selectedStack, client.player == null ? null : client.player.level(), temp, TooltipFlag.Default.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        if(tooltip.isEmpty()){
            return;
        }

        Font textRenderer = client.font;
        List<FormattedCharSequence>orderedToolTip = Lists.transform(tooltip, Component::getVisualOrderText);
        FormattedCharSequence name = orderedToolTip.get(0);

        context.drawString(textRenderer, name, i+10+3-textRenderer.width(name)/2, j-15, -1);
        for(int v=1;v<orderedToolTip.size();v++) {
            FormattedCharSequence text = orderedToolTip.get(v);
            if (text != null) {
                context.drawString(textRenderer, text, i + 10 + 3 - textRenderer.width(text) / 2, j + 15 + 10 * v, -1);
            }
        }
    }

    @Override
    public void closeOpenAccessbar(boolean select){
        if(select){
            this.openAccessbar.selectItem();
        }
        openAccessbar = null;
    }

    @Override
    public void openAccessbar(int num){
        openAccessbar = accessBars[num-1];
        openAccessbar.resetSelection();
    }
    @Override
    public AccessBar getOpenAccessBar() {
        return this.openAccessbar;
    }

    @Override
    public boolean isBarWithNumberOpen(int number){
        return openAccessbar == accessBars[number-1];
    }

    @Override
    public void refreshAccessbars() {
        accessBars = getAccessBarArray();
    }

    private AccessBar[] getAccessBarArray(){
        return new AccessBar[]{
                new AccessBar(WildToolAccessConfig.getClassValue("typeToAccess1"),
                        WildToolAccessSoundEvents.selectInAccess1,
                        accessBarTextureSheets[WildToolAccessConfig.getIntValue("barTexture1")],
                        client),
                new AccessBar(WildToolAccessConfig.getClassValue("typeToAccess2"),
                        WildToolAccessSoundEvents.selectInAccess2,
                        accessBarTextureSheets[WildToolAccessConfig.getIntValue("barTexture2")],
                        client)
        };
    }
}
