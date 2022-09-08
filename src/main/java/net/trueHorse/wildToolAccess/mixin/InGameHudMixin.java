package net.trueHorse.wildToolAccess.mixin;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.AccessBar;
import net.trueHorse.wildToolAccess.GameOptionsAccess;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper implements InGameHudAccess{

    @Shadow
    MinecraftClient client;
    @Shadow
    ItemRenderer itemRenderer;
    @Shadow
    int scaledWidth;
    @Shadow
    int scaledHeight;
    private final List<Identifier> accessBarTextures = List.of(
            new Identifier("wildtoolaccess","textures/gui/access_widgets0.png"),
            new Identifier("wildtoolaccess","textures/gui/access_widgets1.png"));
    private static Identifier accessBarTexture1;
    private static Identifier accessBarTexture2;
    @Final
    private AccessBar accessbar1;
    @Final
    private AccessBar accessbar2;
    private AccessBar openAccessbar;

    @Shadow
    private PlayerEntity getCameraPlayer(){return null;}
    @Shadow
    private void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(MinecraftClient client, CallbackInfo ci){
        setAccessBarTexturesAsConfigured();
        accessbar1 = new AccessBar(1, client);
        accessbar2 = new AccessBar(2, client);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(F Lnet/minecraft/client/util/math/MatrixStack;)V",shift = At.Shift.AFTER))
    public void renderAccessBar(MatrixStack matrices, float tickDelta, CallbackInfo info){
        if(((GameOptionsAccess)client.options).isAccessBarOpen()){
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                if(openAccessbar.getNumber()==1){
                    RenderSystem.setShaderTexture(0, accessBarTexture1);
                }else{
                    RenderSystem.setShaderTexture(0, accessBarTexture2);
                }
                int i = scaledWidth / 2 -10+WildToolAccessConfig.getIntValue("xOffset");
                int j = scaledHeight/2 -54+WildToolAccessConfig.getIntValue("yOffset");
                int m = this.getZOffset();
                this.setZOffset(-90);
                int k;
                int l;
                int distance = 20+WildToolAccessConfig.getIntValue("spaceBetweenSlots");
                
                if(openAccessbar.getStacks().size()==0){
                    this.drawTexture(matrices, i, j, 66, 0, 22, 22);
                }else{
                    for(k = 1; k < openAccessbar.getStacks().size(); ++k) {
                        l = i + k * distance - distance*openAccessbar.getSelectedAccessSlot();
                        this.drawTexture(matrices, l, j, 0, 0, 22, 22);
                    }
                    l = i - distance*openAccessbar.getSelectedAccessSlot();
                    this.drawTexture(matrices, l, j, 22, 0, 22, 22);
                    l = i + k * distance - distance*openAccessbar.getSelectedAccessSlot();
                    this.drawTexture(matrices, l, j, 44, 0, 22, 22);
                }
                this.drawTexture(matrices, i - 1, j - 1, 0, 22, 24, 22);
      
                this.setZOffset(m);

                j += 3;
                int o =1;
                for(k = 0; k < openAccessbar.getStacks().size(); ++k) {
                    l = i + k * distance + 3 - distance*(openAccessbar.getSelectedAccessSlot()-1);
                    this.renderHotbarItem(l, j, tickDelta, playerEntity, openAccessbar.getStacks().get(k),o++);
                }

                String labConf = WildToolAccessConfig.getStringValue("itemInfoShown");
                if(!labConf.equals("non")&&openAccessbar.getSelectedAccessSlot()!=0){
                    renderLabels(matrices, labConf, i, j);
                }
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(MatrixStack matrices,String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlot()-1);
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
                if (selectedStack.hasNbt()) {
                       ItemStack.appendEnchantments(tooltip, selectedStack.getEnchantments());
                }
                if (selectedStack.getItem() instanceof PotionItem){
                    List<Text> temp = new ArrayList<Text>();
                    selectedStack.getItem().appendTooltip(selectedStack, client.player == null ? null : client.player.world, temp, TooltipContext.Default.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        List<OrderedText>orderedToolTip = Lists.transform(tooltip, Text::asOrderedText);
        TextRenderer textRenderer = client.textRenderer;
        OrderedText name = orderedToolTip.get(0);

        textRenderer.drawWithShadow(matrices, name, i+10+3-textRenderer.getWidth(name)/2, j-18, -1);
        for(int v=1;v<orderedToolTip.size();v++){
            OrderedText text = orderedToolTip.get(v);
            if(text!=null){
                textRenderer.drawWithShadow(matrices, text, i+10+3-textRenderer.getWidth(text)/2, j+12+10*v, -1);
            }                    
        }
    }

    public void setAccessBarTexturesAsConfigured(){
        accessBarTexture1 = accessBarTextures.get(WildToolAccessConfig.getIntValue("barTexture1"));
        accessBarTexture2 = accessBarTextures.get(WildToolAccessConfig.getIntValue("barTexture2"));
    }

    @Override
    public void closeOpenAccessbar(boolean select){
        if(select){
            this.openAccessbar.selectItem();
        }
        ((GameOptionsAccess)client.options).setAccessBarOpen(false);
    }

    @Override
    public void openAccessbar(int num){
        switch(num){
            case(1):this.openAccessbar = this.accessbar1;
            break;
            case(2):this.openAccessbar = this.accessbar2;
        }
        openAccessbar.resetSelection();
        ((GameOptionsAccess)client.options).setAccessBarOpen(true);
    }
    @Override
    public AccessBar getOpenAccessBar() {
        return this.openAccessbar;
    }
}
