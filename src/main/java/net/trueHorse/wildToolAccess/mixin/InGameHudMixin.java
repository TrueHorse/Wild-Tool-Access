package net.trueHorse.wildToolAccess.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
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

@Mixin(IngameGui.class)
public class InGameHudMixin extends AbstractGui implements InGameHudAccess{

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
    private PlayerEntity getCameraPlayer(){return null;}
    @Shadow
    private void renderSlot(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(Minecraft client, CallbackInfo ci){
        accessBars = getAccessBarArray();
    }

    //injecting in renderHotbar, because Forge overrides render and I don't want to go trough the effort of registering an overlay
    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void renderAccessBar(float tickDelta, MatrixStack matrixStack, CallbackInfo ci){
        if(openAccessbar!=null){
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                minecraft.getTextureManager().bind(openAccessbar.getTextures());

                int firstSlotXCoordinate = screenWidth / 2 -10+WildToolAccessConfig.xOffset;
                int yCoordinate = screenHeight/2 -54+WildToolAccessConfig.yOffset;
                int m = this.getBlitOffset();
                this.setBlitOffset(-90);

                int xCoordinate;
                int spaceBetweenSlots = 20+WildToolAccessConfig.spaceBetweenSlots;
                
                if(openAccessbar.getStacks().size()==1){
                    blit(matrixStack, firstSlotXCoordinate, yCoordinate, 66, 0, 22, 22);
                }else{
                    int k;
                    for(k = 1; k < openAccessbar.getStacks().size()-1; ++k) {
                        xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                        blit(matrixStack, xCoordinate, yCoordinate, 0, 0, 22, 22);
                    }
                    xCoordinate = firstSlotXCoordinate - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    blit(matrixStack, xCoordinate, yCoordinate, 22, 0, 22, 22);
                    xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    blit(matrixStack, xCoordinate, yCoordinate, 44, 0, 22, 22);
                }
                blit(matrixStack, firstSlotXCoordinate - 1, yCoordinate - 1, 0, 22, 24, 22);

                setBlitOffset(m);
                RenderSystem.enableRescaleNormal();

                for(int i = 0; i < openAccessbar.getStacks().size(); ++i) {
                    xCoordinate = firstSlotXCoordinate + i * spaceBetweenSlots + 3 - spaceBetweenSlots*(openAccessbar.getSelectedAccessSlotIndex());
                    this.renderSlot(xCoordinate, yCoordinate+3, tickDelta, playerEntity, openAccessbar.getStacks().get(i));
                }

                String labConf = WildToolAccessConfig.itemInfoShown;
                if(!labConf.equals("non")&&openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex())!=ItemStack.EMPTY){
                    renderLabels(matrixStack, labConf, firstSlotXCoordinate, yCoordinate);
                }
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(MatrixStack poseStack, String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex());
        List<ITextComponent> tooltip;
        if(labConf.equals("all")){
            tooltip = selectedStack.getTooltipLines(minecraft.player, this.minecraft.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
            tooltip.remove(ITextComponent.EMPTY);
            tooltip.remove((new TranslationTextComponent("item.modifiers.mainhand")).withStyle(TextFormatting.GRAY));
        }else{
            tooltip = new ArrayList<ITextComponent>();
            if(labConf.equals("name")||labConf.equals("enchantments")){
                IFormattableTextComponent name = (new StringTextComponent("")).append(selectedStack.getHoverName()).withStyle(selectedStack.getRarity().color);
                if (selectedStack.hasCustomHoverName()) {
                    name.withStyle(TextFormatting.ITALIC);
                }
                tooltip.add(name);
            }
    
            if(labConf.equals("enchantments")){
                if (selectedStack.hasTag()) {
                       ItemStack.appendEnchantmentNames(tooltip, selectedStack.getEnchantmentTags());
                }
                if (selectedStack.getItem() instanceof PotionItem){
                    List<ITextComponent> temp = new ArrayList<ITextComponent>();
                    selectedStack.getItem().appendHoverText(selectedStack, minecraft.player == null ? null : minecraft.player.level, temp, ITooltipFlag.TooltipFlags.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        if(tooltip.isEmpty()){
            return;
        }

        FontRenderer textRenderer = minecraft.font;
        List<IReorderingProcessor>orderedToolTip = Lists.transform(tooltip, ITextComponent::getVisualOrderText);
        IReorderingProcessor name = orderedToolTip.get(0);

        textRenderer.drawShadow(poseStack,name, i+10+3-textRenderer.width(name)/2, j-15, -1);
        for(int v=1;v<orderedToolTip.size();v++) {
            IReorderingProcessor text = orderedToolTip.get(v);
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
