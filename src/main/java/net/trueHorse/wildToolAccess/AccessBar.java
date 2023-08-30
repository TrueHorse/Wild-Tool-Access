package net.trueHorse.wildToolAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.trueHorse.wildToolAccess.config.StuffHandler;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;

public class AccessBar{

    private final Minecraft client;
    private final Class<?> classToAccess;
    private final SoundEvent selectionSoundEvent;
    private ArrayList<ItemStack> stacks;
    private int selectedAccessSlotIndex = 0;
    private ItemStack lastSwappedOutTool =ItemStack.EMPTY;
    private final ResourceLocation textures;

    public AccessBar(Class<?> classToAccess, SoundEvent selectionSoundEvent, ResourceLocation textures, Minecraft client){
        this.client = client;
        this.classToAccess = classToAccess;
        this.selectionSoundEvent = selectionSoundEvent;
        this.textures = textures;
    }

    public void updateAccessStacks(){
        PlayerInventory inv = client.player.inventory;
        stacks = WildToolAccessConfig.leadingEmptySlot ? new ArrayList<>(Collections.singletonList(ItemStack.EMPTY)) : new ArrayList<>();


        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        if(!classToAccess.equals(StuffPlaceholder.class)){
            itemStacks.addAll(((PlayerInventoryAccess)inv).getAllMainStacksOfType(classToAccess));
        }else{
            itemStacks.addAll(((PlayerInventoryAccess)inv).getAllMainStacksOf(StuffHandler.getStuffItems()));
        }

        if(WildToolAccessConfig.lastSwappedOutFirst){
            int prioStackSlot = inv.findSlotMatchingItem(lastSwappedOutTool);
            ItemStack prioStack = prioStackSlot == -1 ? ItemStack.EMPTY : inv.getItem(prioStackSlot);
            if(prioStack!=ItemStack.EMPTY){
                stacks.add(prioStack);
                itemStacks.remove(prioStack);
            }
        }

        if(stacks.isEmpty()){
            stacks.add(ItemStack.EMPTY);
        }

        stacks.addAll(itemStacks);
    }

    public void scrollInAccessBar(double scrollAmount) {
        int barSize = stacks.size();
        int slotsToScroll = (int)Math.signum(scrollAmount);
  
        selectedAccessSlotIndex = Math.floorMod(selectedAccessSlotIndex -slotsToScroll, barSize);
    }

    public void selectItem(){
        PlayerInventory inv = client.player.inventory;
        int slotSwapIsLockedTo = WildToolAccessConfig.lockSwappingToSlot;
        int slotToSwap = !(slotSwapIsLockedTo<1||slotSwapIsLockedTo>PlayerInventory.getSelectionSize()) ? slotSwapIsLockedTo-1 : inv.selected;
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
            if(!(hotbarSlotToSelect<1||hotbarSlotToSelect>PlayerInventory.getSelectionSize())){
                inv.selected = hotbarSlotToSelect-1;
            }

            client.getSoundManager().play(SimpleSound.forUI(selectionSoundEvent,1.0F,1.0F));
 
            if(classToAccess.isAssignableFrom(selectedHotbarSlotStack.getItem().getClass())){
                lastSwappedOutTool = selectedHotbarSlotStack.copy();
            }
        }
    }

    public void resetSelection(){
        PlayerInventory inv = client.player.inventory;
        if(WildToolAccessConfig.heldItemSelected&&classToAccess.isAssignableFrom(inv.getItem(inv.selected).getItem().getClass())){
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