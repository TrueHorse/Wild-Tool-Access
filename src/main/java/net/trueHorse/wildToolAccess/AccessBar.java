package net.trueHorse.wildToolAccess;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class AccessBar{

    private final MinecraftClient client;
    private final Class<?> classToAccess;
    private final SoundEvent selectionSoundEvent;
    private ArrayList<ItemStack> stacks;
    private int selectedAccessSlotIndex = 0;
    private ItemStack lastSwappedOutTool =ItemStack.EMPTY;
    private final Identifier textures;

    public AccessBar(Class<?> classToAccess, SoundEvent selectionSoundEvent, Identifier textures, MinecraftClient client){
        this.client = client;
        this.classToAccess = classToAccess;
        this.selectionSoundEvent = selectionSoundEvent;
        this.textures = textures;
    }

    public void updateAccessStacks(){
        PlayerInventory inv = client.player.inventory;
        stacks = WildToolAccessConfig.getBoolValue("leadingEmptySlot") ? new ArrayList<>(Arrays.asList(ItemStack.EMPTY)) : new ArrayList<>();


        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        if(!classToAccess.equals(StuffPlaceholder.class)){
            itemStacks.addAll(((PlayerInventoryAccess)inv).getAllMainStacksOfType(classToAccess));
        }else{
            itemStacks.addAll(((PlayerInventoryAccess)inv).getAllMainStacksOf(WildToolAccessConfig.getStuffItems()));
        }

        if(WildToolAccessConfig.getBoolValue("lastSwappedOutFirst")){
            int prioStackSlot = inv.getSlotWithStack(lastSwappedOutTool);
            ItemStack prioStack = prioStackSlot == -1 ? ItemStack.EMPTY : inv.getStack(prioStackSlot);
            if(prioStack!=ItemStack.EMPTY){
                stacks.add(prioStack);
                itemStacks.remove(prioStack);
            }
        }

        if(itemStacks.isEmpty()){
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
        int slotSwapIsLockedTo = WildToolAccessConfig.getIntValue("lockSwappingToSlot");
        int slotToSwap = !(slotSwapIsLockedTo<1||slotSwapIsLockedTo>PlayerInventory.getHotbarSize()) ? slotSwapIsLockedTo-1 : inv.selectedSlot;
        ItemStack selectedHotbarSlotStack = inv.getStack(slotToSwap);
        ItemStack selectedAccessbarStack = stacks.get(selectedAccessSlotIndex);

        if(selectedAccessbarStack!=ItemStack.EMPTY&&!(ItemStack.areEqual(selectedHotbarSlotStack, selectedAccessbarStack))){
            int accessbarStackPos = inv.main.indexOf(selectedAccessbarStack);
            int slotToTheRight = (slotToSwap+1)%9;
            boolean putToTheRight = (WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))&&(inv.getStack(slotToTheRight) == ItemStack.EMPTY);
            BiConsumer<Integer, Integer> swapSlots = ((slot1, slot2)->client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,slot1, slot2, SlotActionType.SWAP,client.player));

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

            int hotbarSlotToSelect = WildToolAccessConfig.getIntValue("hotbarSlotAfterSwap");
            if(!(hotbarSlotToSelect<1||hotbarSlotToSelect>PlayerInventory.getHotbarSize())){
                inv.selectedSlot = hotbarSlotToSelect-1;
            }

            client.getSoundManager().play(PositionedSoundInstance.master(selectionSoundEvent,1.0F,1.0F));
 
            if(classToAccess.isAssignableFrom(selectedHotbarSlotStack.getItem().getClass())){
                lastSwappedOutTool = selectedHotbarSlotStack.copy();
            }
        }
    }

    public void resetSelection(){
        PlayerInventory inv = client.player.inventory;
        if(WildToolAccessConfig.getBoolValue("heldItemSelected")&&classToAccess.isAssignableFrom(inv.getStack(inv.selectedSlot).getItem().getClass())){
            updateAccessStacks();
            selectedAccessSlotIndex = stacks.indexOf(inv.getStack(inv.selectedSlot));
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

    public Identifier getTextures() {
        return textures;
    }
}