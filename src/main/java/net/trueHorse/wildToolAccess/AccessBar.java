package net.trueHorse.wildToolAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.trueHorse.wildToolAccess.config.ItemTypeHandler;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class AccessBar{

    private final Minecraft client;
    private final String accessType;
    private final SoundEvent selectionSoundEvent;
    private final ArrayList<ItemStack> stacks = new ArrayList<>();
    private int selectedAccessSlotIndex = 0;
    private ItemStack lastSwappedOutTool =ItemStack.EMPTY;
    private final ResourceLocation textures;

    public AccessBar(String accessType, SoundEvent selectionSoundEvent, ResourceLocation textures, Minecraft client){
        this.client = client;
        this.accessType = accessType;
        this.selectionSoundEvent = selectionSoundEvent;
        this.textures = textures;
    }

    public void updateAccessStacks(){
        Inventory inv = client.player.getInventory();
        stacks.clear();

        ArrayList<ItemStack> itemStacks = new ArrayList<>(((PlayerInventoryAccess) inv).getAllMainStacksOfType(accessType));

        if(itemStacks.isEmpty()||WildToolAccessConfig.leadingEmptySlot){
            stacks.add(ItemStack.EMPTY);
        }

        if(WildToolAccessConfig.lastSwappedOutFirst){
            int prioStackSlot = inv.findSlotMatchingItem(lastSwappedOutTool);
            ItemStack prioStack = prioStackSlot == -1 ? ItemStack.EMPTY : inv.getItem(prioStackSlot);
            if(prioStack!=ItemStack.EMPTY){
                stacks.add(prioStack);
                itemStacks.remove(prioStack);
            }
        }

        stacks.addAll(itemStacks);
    }

    public void scrollInAccessBar(double scrollAmount) {
        int barSize = stacks.size();
        int slotsToScroll = (int)Math.signum(scrollAmount);
  
        selectedAccessSlotIndex = Math.floorMod(selectedAccessSlotIndex -slotsToScroll, barSize);
    }

    public void selectItem(){
        Inventory inv = client.player.getInventory();
        int slotSwapIsLockedTo = WildToolAccessConfig.lockSwappingToSlot;
        int slotToSwap = !(slotSwapIsLockedTo<1||slotSwapIsLockedTo>Inventory.getSelectionSize()) ? slotSwapIsLockedTo-1 : inv.selected;
        ItemStack selectedHotbarSlotStack = inv.getItem(slotToSwap);
        ItemStack selectedAccessbarStack = stacks.get(selectedAccessSlotIndex);

        if(selectedAccessbarStack!=ItemStack.EMPTY&&!(ItemStack.matches(selectedHotbarSlotStack, selectedAccessbarStack))){
            int accessbarStackPos = inv.items.indexOf(selectedAccessbarStack);
            int slotToTheRight = (slotToSwap+1)%9;
            boolean putToTheRight = (WildToolAccessConfig.putToTheRightIfPossible)&&(inv.getItem(slotToTheRight) == ItemStack.EMPTY);
            BiConsumer<Integer, Integer> swapSlots = ((slot1, slot2)->client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId,slot1, slot2, ClickType.SWAP,client.player));

            if(accessbarStackPos<9){
                swapSlots.accept(9,slotToSwap);

                if(putToTheRight){
                    swapSlots.accept(9,slotToTheRight);
                }
                swapSlots.accept(9,accessbarStackPos);
                swapSlots.accept(9,slotToSwap);
            }else{
                swapSlots.accept(accessbarStackPos,slotToSwap);

                if(putToTheRight){
                    swapSlots.accept(accessbarStackPos,slotToTheRight);
                }
            }

            int hotbarSlotToSelect = WildToolAccessConfig.hotbarSlotAfterSwap;
            if(!(hotbarSlotToSelect<1||hotbarSlotToSelect>Inventory.getSelectionSize())){
                inv.selected = hotbarSlotToSelect-1;
            }

            client.getSoundManager().play(SimpleSoundInstance.forUI(selectionSoundEvent,1.0F,1.0F));
 
            if(ItemTypeHandler.getItemType(accessType).contains(selectedAccessbarStack.getItem())){
                lastSwappedOutTool = selectedHotbarSlotStack.copy();
            }
        }
    }

    public void resetSelection(){
        Inventory inv = client.player.getInventory();
        if(WildToolAccessConfig.heldItemSelected&&ItemTypeHandler.getItemType(accessType).contains(inv.getItem(inv.selected).getItem())){
            updateAccessStacks();
            selectedAccessSlotIndex = stacks.indexOf(inv.getItem(inv.selected));
        }else{
            selectedAccessSlotIndex = 0;
        }
    }

    public int getSelectedAccessSlotIndex() {
        return selectedAccessSlotIndex;
    }

    public void setSelectedAccessSlotIndex(int selectedAccessSlotIndex) {
        this.selectedAccessSlotIndex = selectedAccessSlotIndex;
    }

    public ArrayList<ItemStack> getStacks() {
        return stacks;
    }

    public ResourceLocation getTextures() {
        return textures;
    }
}