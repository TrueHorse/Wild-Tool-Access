package net.trueHorse.wildToolAccess.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper implements InGameHudAccess{

    @Final @Shadow
    private MinecraftClient client;
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;
    private final Identifier[] accessBarTextureSheets = {
            new Identifier("wildtoolaccess","textures/gui/access_widgets0.png"),
            new Identifier("wildtoolaccess","textures/gui/access_widgets1.png")};
    private AccessBar[] accessBars;
    private AccessBar openAccessbar;

    @Shadow
    private PlayerEntity getCameraPlayer(){return null;}
    @Shadow
    private void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(MinecraftClient client, ItemRenderer itemRenderer, CallbackInfo ci){
        accessBars = getAccessBarArray();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(F Lnet/minecraft/client/util/math/MatrixStack;)V",shift = At.Shift.AFTER))
    public void renderAccessBar(MatrixStack matrices, float tickDelta, CallbackInfo info){
        if(openAccessbar!=null){
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                client.getTextureManager().bindTexture(openAccessbar.getTextures());

                int firstSlotXCoordinate = scaledWidth / 2 -10+WildToolAccessConfig.getIntValue("xOffset");
                int yCoordinate = scaledHeight/2 -54+WildToolAccessConfig.getIntValue("yOffset");
                int m = this.getZOffset();
                this.setZOffset(-90);

                int xCoordinate;
                int spaceBetweenSlots = 20+WildToolAccessConfig.getIntValue("spaceBetweenSlots");
                
                if(openAccessbar.getStacks().size()==0){
                    drawTexture(matrices, firstSlotXCoordinate, yCoordinate, 66, 0, 22, 22);
                }else{
                    int k;
                    for(k = 1; k < openAccessbar.getStacks().size(); ++k) {
                        xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                        drawTexture(matrices, xCoordinate, yCoordinate, 0, 0, 22, 22);
                    }
                    xCoordinate = firstSlotXCoordinate - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    drawTexture(matrices, xCoordinate, yCoordinate, 22, 0, 22, 22);
                    xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    drawTexture(matrices, xCoordinate, yCoordinate, 44, 0, 22, 22);
                }
                drawTexture(matrices, firstSlotXCoordinate - 1, yCoordinate - 1, 0, 22, 24, 22);

                this.setZOffset(m);
                RenderSystem.enableRescaleNormal();

                for(int i = 0; i < openAccessbar.getStacks().size(); ++i) {
                    xCoordinate = firstSlotXCoordinate + i * spaceBetweenSlots + 3 - spaceBetweenSlots*(openAccessbar.getSelectedAccessSlotIndex());
                    this.renderHotbarItem(xCoordinate, yCoordinate, tickDelta, playerEntity, openAccessbar.getStacks().get(i));
                }

                String labConf = WildToolAccessConfig.getStringValue("itemInfoShown");
                if(!labConf.equals("non")&&openAccessbar.getSelectedAccessSlotIndex()!=0){
                    renderLabels(matrices, labConf, firstSlotXCoordinate, yCoordinate);
                }
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(MatrixStack matrices,String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex());
        List<Text> tooltip;
        if(labConf.equals("all")){
            tooltip = selectedStack.getTooltip(client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
            tooltip.remove(LiteralText.EMPTY);
            tooltip.remove((new TranslatableText("item.modifiers.mainhand")).formatted(Formatting.GRAY));
        }else{
            tooltip = new ArrayList<Text>();
            if(labConf.equals("name")||labConf.equals("enchantments")){
                MutableText name = (new LiteralText("")).append(selectedStack.getName()).formatted(selectedStack.getRarity().formatting);
                if (selectedStack.hasCustomName()) {
                    name.formatted(Formatting.ITALIC);
                }
                tooltip.add(name);
            }
    
            if(labConf.equals("enchantments")){
                if (selectedStack.hasTag()) {
                       ItemStack.appendEnchantments(tooltip, selectedStack.getEnchantments());
                }
                if (selectedStack.getItem() instanceof PotionItem){
                    List<Text> temp = new ArrayList<Text>();
                    selectedStack.getItem().appendTooltip(selectedStack, client.player == null ? null : client.player.world, temp, TooltipContext.Default.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        if(tooltip.isEmpty()){
            return;
        }

        List<OrderedText>orderedToolTip = Lists.transform(tooltip, Text::asOrderedText);
        TextRenderer textRenderer = client.textRenderer;
        OrderedText name = orderedToolTip.get(0);

        textRenderer.drawWithShadow(matrices, name, i+10+3-textRenderer.getWidth(name)/2, j-15, -1);
        for(int v=1;v<orderedToolTip.size();v++){
            OrderedText text = orderedToolTip.get(v);
            if(text!=null){
                textRenderer.drawWithShadow(matrices, text, i+10+3-textRenderer.getWidth(text)/2, j+15+10*v, -1);
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
