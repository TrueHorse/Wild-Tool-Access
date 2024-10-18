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
import net.trueHorse.wildToolAccess.duck.InGameHudAccess;
import net.trueHorse.wildToolAccess.WildToolAccessSoundEvents;
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
    private final Identifier[] accessBarTextureSheets = {
        new Identifier("wildtoolaccess","textures/gui/access_widgets0.png"),
        new Identifier("wildtoolaccess","textures/gui/access_widgets1.png")};
    private AccessBar[] accessBars;
    private AccessBar openAccessbar;

    @Shadow
    private PlayerEntity getCameraPlayer(){return null;}
    @Shadow
    private void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed){}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAccessBar(MinecraftClient client, ItemRenderer itemRenderer, CallbackInfo ci){
        accessBars = getAccessBarArray();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/gui/DrawContext;)V",shift = At.Shift.AFTER))
    public void renderAccessBar(DrawContext context, float tickDelta, CallbackInfo ci){
        if(openAccessbar!=null){
            PlayerEntity playerEntity = this.getCameraPlayer();
            if (playerEntity != null) {
                openAccessbar.updateAccessStacks();

                Identifier barTextures;
                barTextures = openAccessbar.getTextures();

                int firstSlotXCoordinate = scaledWidth / 2 -10+WildToolAccessConfig.getIntValue("xOffset");
                int yCoordinate = scaledHeight/2 -54+WildToolAccessConfig.getIntValue("yOffset");
                context.getMatrices().push();
                context.getMatrices().translate(0.0F, 0.0F, -90.0F);

                int xCoordinate;
                int spaceBetweenSlots = 20+WildToolAccessConfig.getIntValue("spaceBetweenSlots");
                
                if(openAccessbar.getStacks().size()==1){
                    context.drawTexture(barTextures, firstSlotXCoordinate, yCoordinate, 66, 0, 22, 22);
                }else{
                    int k;
                    for(k = 1; k < openAccessbar.getStacks().size()-1; ++k) {
                        xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                        context.drawTexture(barTextures, xCoordinate, yCoordinate, 0, 0, 22, 22);
                    }
                    xCoordinate = firstSlotXCoordinate - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    context.drawTexture(barTextures, xCoordinate, yCoordinate, 22, 0, 22, 22);
                    xCoordinate = firstSlotXCoordinate + k * spaceBetweenSlots - spaceBetweenSlots*openAccessbar.getSelectedAccessSlotIndex();
                    context.drawTexture(barTextures, xCoordinate, yCoordinate, 44, 0, 22, 22);
                }
                context.drawTexture(barTextures, firstSlotXCoordinate - 1, yCoordinate - 1, 0, 22, 24, 22);

                context.getMatrices().pop();

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

    private void renderLabels(DrawContext context,String labConf, int i, int j){
        ItemStack selectedStack = openAccessbar.getStacks().get(openAccessbar.getSelectedAccessSlotIndex());
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

        if(tooltip.isEmpty()){
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        List<OrderedText>orderedToolTip = Lists.transform(tooltip, Text::asOrderedText);
        OrderedText name = orderedToolTip.get(0);

        context.drawTextWithShadow(textRenderer, name, i+10+3-textRenderer.getWidth(name)/2, j-15, -1);
        for(int v=1;v<orderedToolTip.size();v++) {
            OrderedText text = orderedToolTip.get(v);
            if (text != null) {
                context.drawTextWithShadow(textRenderer, text, i + 10 + 3 - textRenderer.getWidth(text) / 2, j + 15 + 10 * v, -1);
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
                new AccessBar(WildToolAccessConfig.getStringValue("typeToAccess1"),
                        WildToolAccessSoundEvents.selectInAccess1,
                        accessBarTextureSheets[WildToolAccessConfig.getIntValue("barTexture1")],
                        client),
                new AccessBar(WildToolAccessConfig.getStringValue("typeToAccess2"),
                        WildToolAccessSoundEvents.selectInAccess2,
                        accessBarTextureSheets[WildToolAccessConfig.getIntValue("barTexture2")],
                        client)
        };
    }
}
