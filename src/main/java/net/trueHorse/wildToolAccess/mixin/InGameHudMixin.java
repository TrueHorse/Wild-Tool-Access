package net.trueHorse.wildToolAccess.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public class InGameHudMixin extends GuiComponent implements InGameHudAccess{

    @Final @Shadow
    protected Minecraft minecraft;
    @Shadow
    protected int screenWidth;
    @Shadow
    protected int screenHeight;
    @Unique
    private final ResourceLocation[] accessBarTextureSheets = accessBarTextureSheets();
    @Unique
    private ResourceLocation[] accessBarTextureSheets(){
        return new ResourceLocation[]{
                new ResourceLocation("wildtoolaccess", "textures/gui/access_widgets0.png"),
                new ResourceLocation("wildtoolaccess", "textures/gui/access_widgets1.png")};
    }
    @Unique
    private AccessBar[] accessBars;
    @Unique
    private AccessBar openAccessbar;

    @Shadow
    private Player getCameraPlayer(){return null;}
    @Shadow
    private void renderSlot(int x, int y, float tickDelta, Player player, ItemStack stack, int seed){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(Minecraft client, CallbackInfo ci){
        accessBars = getAccessBarArray();
    }

    //injecting in renderHotbar, because Forge overrides render and I don't want to go trough the effort of registering an overlay
    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void renderAccessBar(float tickDelta, PoseStack poseStack, CallbackInfo ci){
        if(openAccessbar!=null){
            Player playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.setShaderTexture(0,openAccessbar.getTextures());

                int firstSlotXCoordinate = screenWidth / 2 -10+WildToolAccessConfig.xOffset;
                int yCoordinate = screenHeight/2 -54+WildToolAccessConfig.yOffset;
                int m = this.getBlitOffset();
                this.setBlitOffset(-90);

                int xCoordinate;
                int spaceBetweenSlots = 20+WildToolAccessConfig.spaceBetweenSlots;
                
                if(openAccessbar.getStacks().size()==1){
                    blit(poseStack, firstSlotXCoordinate, yCoordinate, 66, 0, 22, 22);
                }else{
                    int k;
                    for(k = 1; k < openAccessbar.getStacks().size()-1; ++k) {
                        xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                        blit(poseStack, xCoordinate, yCoordinate, 0, 0, 22, 22);
                    }
                    xCoordinate = firstSlotXCoordinate - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    blit(poseStack, xCoordinate, yCoordinate, 22, 0, 22, 22);
                    xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    blit(poseStack, xCoordinate, yCoordinate, 44, 0, 22, 22);
                }
                blit(poseStack, firstSlotXCoordinate - 1, yCoordinate - 1, 0, 22, 24, 22);

                setBlitOffset(m);

                int seed =1;
                for(int i = 0; i < openAccessbar.getStacks().size(); ++i) {
                    xCoordinate = firstSlotXCoordinate + i * spaceBetweenSlots + 3 - spaceBetweenSlots*(openAccessbar.getSelectedAccessSlotIndex());
                    this.renderSlot(xCoordinate, yCoordinate+3, tickDelta, playerEntity, openAccessbar.getStacks().get(i),seed++);
                }

                String labConf = WildToolAccessConfig.itemInfoShown;
                if(!labConf.equals("non")&&openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex())!=ItemStack.EMPTY){
                    renderLabels(poseStack, labConf, firstSlotXCoordinate, yCoordinate);
                }
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(PoseStack poseStack, String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex());
        List<Component> tooltip;
        if(labConf.equals("all")){
            tooltip = selectedStack.getTooltipLines(minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            tooltip.remove(TextComponent.EMPTY);
            tooltip.remove((new TranslatableComponent("item.modifiers.mainhand")).withStyle(ChatFormatting.GRAY));
        }else{
            tooltip = new ArrayList<Component>();
            if(labConf.equals("name")||labConf.equals("enchantments")){
                MutableComponent name = (new TextComponent("")).append(selectedStack.getHoverName()).withStyle(selectedStack.getRarity().getStyleModifier());
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
                    selectedStack.getItem().appendHoverText(selectedStack, minecraft.player == null ? null : minecraft.player.level, temp, TooltipFlag.Default.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        if(tooltip.isEmpty()){
            return;
        }

        Font textRenderer = minecraft.font;
        List<FormattedCharSequence>orderedToolTip = Lists.transform(tooltip, Component::getVisualOrderText);
        FormattedCharSequence name = orderedToolTip.get(0);

        textRenderer.drawShadow(poseStack,name, i+10+3-textRenderer.width(name)/2, j-15, -1);
        for(int v=1;v<orderedToolTip.size();v++) {
            FormattedCharSequence text = orderedToolTip.get(v);
            if (text != null) {
                textRenderer.drawShadow(poseStack, text, i + 10 + 3 - textRenderer.width(text) / 2, j + 15 + 10 * v, -1);
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
                new AccessBar(WildToolAccessConfig.typeToAccess1,
                        WildToolAccessSoundEvents.selectInAccess1,
                        accessBarTextureSheets[WildToolAccessConfig.barTexture1],
                        minecraft),
                new AccessBar(WildToolAccessConfig.typeToAccess2,
                        WildToolAccessSoundEvents.selectInAccess2,
                        accessBarTextureSheets[WildToolAccessConfig.barTexture2],
                        minecraft)
        };
    }
}
