package net.trueHorse.wildToolAccess;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class AccessBar{
    
    private final PlayerInventory inv;
    private final MinecraftClient client;
    private final Class<?> classToAccess;
    private final SoundEvent selectionSoundEvent;
    private ArrayList<ItemStack> stacks;
    private int selectedAccessSlot = 0;
    private ItemStack lastSwappedOutTool =ItemStack.EMPTY;
    private Identifier textures;

    public AccessBar(Class<?> classToAccess, SoundEvent selectionSoundEvent, Identifier textures, MinecraftClient client){
        this.client = client;
        this.inv = client.player.getInventory();
        this.classToAccess = classToAccess;
        this.selectionSoundEvent = selectionSoundEvent;
        this.textures = textures;
    }

    public void updateAccessStacks(){
        if(!classToAccess.equals(StuffPlaceholder.class)){
            stacks = ((PlayerInventoryAccess)inv).getAllMainStacksOfType(classToAccess);
        }else{
            stacks = ((PlayerInventoryAccess)inv).getAllMainStacksOf(WildToolAccessConfig.getStuffItems());
        }
        if(WildToolAccessConfig.getBoolValue("lastSwappedOutFirst")){
            int prioStackSlot = inv.getSlotWithStack(lastSwappedOutTool);
            ItemStack prioStack = prioStackSlot == -1 ? ItemStack.EMPTY : inv.getStack(prioStackSlot);
            if(prioStack!=ItemStack.EMPTY){
                ArrayList<ItemStack> temp = new ArrayList<ItemStack>();
                temp.add(prioStack);
                stacks.remove(prioStack);
                temp.addAll(stacks);
                stacks = temp;
            }
        }
    }

    public void scrollInAccessBar(double scrollAmount) {
        int barSize = stacks.size()+1;
        int slotsToScroll = (int)Math.signum(scrollAmount);
  
        selectedAccessSlot = (selectedAccessSlot+slotsToScroll)%barSize;
    }

    public void selectItem(){
        int slotSwapIsLockedTo = WildToolAccessConfig.getIntValue("lockSwappingToSlot");
        int slotToSwap = !(slotSwapIsLockedTo<1||slotSwapIsLockedTo>PlayerInventory.getHotbarSize()) ? slotSwapIsLockedTo-1 : inv.selectedSlot;
        ItemStack selectedHotbarSlotStack = inv.getStack(slotToSwap);
        ItemStack selectedAccessbarStack = stacks.get(selectedAccessSlot-1);

        if(selectedAccessSlot!=0&&!(ItemStack.areEqual(selectedHotbarSlotStack, selectedAccessbarStack))){
            int selectedToolPos = inv.main.indexOf(selectedAccessbarStack);
            int slotToTheRight = (slotToSwap+1)%9;
            boolean putToTheRight = (WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))&&(inv.getStack(slotToTheRight) == ItemStack.EMPTY);

            BiConsumer<Integer, Integer> swapSlots = ((slot1, slot2)->client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,slot1, slot2, SlotActionType.SWAP,client.player));

            if(selectedToolPos<9){
                swapSlots.accept(9,slotToSwap);

                if(putToTheRight){
                    swapSlots.accept(9,slotToTheRight);
                }
                swapSlots.accept(9,selectedToolPos);
                swapSlots.accept(9,slotToSwap);
            }else{
                swapSlots.accept(selectedToolPos,slotToSwap);

                if(putToTheRight){
                    swapSlots.accept(selectedToolPos,slotToTheRight);
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
        selectedAccessSlot = 0;
    }

    public int getSelectedAccessSlot() {
        return selectedAccessSlot;
    }

    public ArrayList<ItemStack> getStacks() {
        return stacks;
    }
}
