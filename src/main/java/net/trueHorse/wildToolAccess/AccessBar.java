package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

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
            if(prioStack!=ItemStack.EMPTY && inv.contains(prioStack)){
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
        if (scrollAmount > 0.0D) {
           scrollAmount = 1.0D;
        }
  
        if (scrollAmount < 0.0D) {
           scrollAmount = -1.0D;
        }
  
        for(this.selectedAccessSlot = (int)((double)this.selectedAccessSlot - scrollAmount); this.selectedAccessSlot < 0; this.selectedAccessSlot += barSize) {
        }
  
        while(this.selectedAccessSlot >= barSize) {
           this.selectedAccessSlot -= barSize;
        }
    }

    public void selectItem(){
        int slotSwapIsLockedTo = WildToolAccessConfig.getIntValue("lockSwappingToSlot");
        int slotToSwap = !(slotSwapIsLockedTo<1||slotSwapIsLockedTo>PlayerInventory.getHotbarSize()) ? slotSwapIsLockedTo-1 : inv.selectedSlot;

        if(selectedAccessSlot!=0&&!(ItemStack.areEqual(inv.getStack(slotToSwap), stacks.get(selectedAccessSlot-1)))){
            int selectedToolPos = inv.main.indexOf(stacks.get(selectedAccessSlot-1));
            int slotToTheRight = (slotToSwap+1)%9;
            boolean putToTheRight = (WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))&&(inv.getStack(slotToTheRight) == ItemStack.EMPTY);
            ItemStack selectedHotbarSlotStack = inv.getStack(slotToSwap);

            if(selectedToolPos<9){
                client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,9,slotToSwap, SlotActionType.SWAP,client.player);

                if(putToTheRight){
                    client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,9,slotToTheRight, SlotActionType.SWAP,client.player);
                }
                client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,9,selectedToolPos, SlotActionType.SWAP,client.player);
                client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,9,slotToSwap, SlotActionType.SWAP,client.player);
            }else{
                client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,selectedToolPos,slotToSwap, SlotActionType.SWAP,client.player);

                if(putToTheRight){
                    client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId,selectedToolPos,slotToTheRight, SlotActionType.SWAP,client.player);
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
