package net.trueHorse.wildToolAccess.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.AccessBar;
import net.trueHorse.wildToolAccess.InGameHudAccess;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.include.com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin implements InGameHudAccess{

    @Final @Shadow
    private MinecraftClient client;
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;
    private final List<Identifier> accessBarTextures = List.of(
        new Identifier("wildtoolaccess","textures/gui/access_widgets0.png"),
        new Identifier("wildtoolaccess","textures/gui/access_widgets1.png"));
    private Identifier accessBarTexture1;
    private Identifier accessBarTexture2;
    @Final
    private AccessBar accessbar1;
    @Final
    private AccessBar accessbar2;
    private AccessBar openAccessbar;

    @Shadow
    private PlayerEntity getCameraPlayer(){return null;}
    @Shadow
    private void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(MinecraftClient client, ItemRenderer itemRenderer, CallbackInfo ci){
        setAccessBarTexturesAsConfigured();
        accessbar1 = new AccessBar(1, client);
        accessbar2 = new AccessBar(2, client);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V",shift = At.Shift.AFTER))
    public void renderAccessBar(DrawContext context, float tickDelta, CallbackInfo ci){
        if(openAccessbar!=null){
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                Identifier barTextures;
                if(openAccessbar.getNumber()==1){
                    barTextures = accessBarTexture1;
                }else{
                    barTextures = accessBarTexture2;
                }
                int i = scaledWidth / 2 -10+WildToolAccessConfig.getIntValue("xOffset");
                int j = scaledHeight/2 -54+WildToolAccessConfig.getIntValue("yOffset");
                context.getMatrices().push();
                context.getMatrices().translate(0.0F, 0.0F, -90.0F);
                int k;
                int l;
                int distance = 20+WildToolAccessConfig.getIntValue("spaceBetweenSlots");
                
                if(openAccessbar.getStacks().size()==0){
                    context.drawTexture(barTextures, i, j, 66, 0, 22, 22);
                }else{
                    for(k = 1; k < openAccessbar.getStacks().size(); ++k) {
                        l = i + k * distance - distance*openAccessbar.getSelectedAccessSlot();
                        context.drawTexture(barTextures, l, j, 0, 0, 22, 22);
                    }
                    l = i - distance*openAccessbar.getSelectedAccessSlot();
                    context.drawTexture(barTextures, l, j, 22, 0, 22, 22);
                    l = i + k * distance - distance*openAccessbar.getSelectedAccessSlot();
                    context.drawTexture(barTextures, l, j, 44, 0, 22, 22);
                }
                context.drawTexture(barTextures, i - 1, j - 1, 0, 22, 24, 22);

                context.getMatrices().pop();

                j += 3;
                int o =1;
                for(k = 0; k < openAccessbar.getStacks().size(); ++k) {
                    l = i + k * distance + 3 - distance*(openAccessbar.getSelectedAccessSlot()-1);
                    this.renderHotbarItem(context, l, j, tickDelta, playerEntity, openAccessbar.getStacks().get(k),o++);
                }

                String labConf = WildToolAccessConfig.getStringValue("itemInfoShown");
                if(!labConf.equals("non")&&openAccessbar.getSelectedAccessSlot()!=0){
                    renderLabels(context, labConf, i, j);
                }
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderLabels(DrawContext context,String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlot()-1);
        List<Text> tooltip;
        if(labConf.equals("all")){
            tooltip = selectedStack.getTooltip(client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
            tooltip.remove(ScreenTexts.EMPTY);
            tooltip.remove((Text.translatable("item.modifiers.mainhand")).formatted(Formatting.GRAY));
        }else{
            tooltip = new ArrayList<Text>();
            if(labConf.equals("name")||labConf.equals("enchantments")){
                MutableText name = (Text.literal("")).append(selectedStack.getName()).formatted(selectedStack.getRarity().formatting);
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
                    selectedStack.getItem().appendTooltip(selectedStack, client.player == null ? null : client.player.getWorld(), temp, TooltipContext.Default.ADVANCED);
                    tooltip.add(temp.get(0));
                }
            }
        }

        TextRenderer textRenderer = client.textRenderer;
        List<OrderedText>orderedToolTip = Lists.transform(tooltip, Text::asOrderedText);
        OrderedText name = orderedToolTip.get(0);

        context.drawTextWithShadow(textRenderer, name, i+10+3-textRenderer.getWidth(name)/2, j-18, -1);
        for(int v=1;v<orderedToolTip.size();v++) {
            OrderedText text = orderedToolTip.get(v);
            if (text != null) {
                context.drawTextWithShadow(textRenderer, text, i + 10 + 3 - textRenderer.getWidth(text) / 2, j + 12 + 10 * v, -1);
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
        openAccessbar = null;
    }

    @Override
    public void openAccessbar(int num){
        switch(num){
            case(1):this.openAccessbar = this.accessbar1;
            break;
            case(2):this.openAccessbar = this.accessbar2;
        }
        openAccessbar.resetSelection();
    }
    @Override
    public AccessBar getOpenAccessBar() {
        return this.openAccessbar;
    }
}
