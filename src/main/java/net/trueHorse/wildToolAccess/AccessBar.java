package net.trueHorse.wildToolAccess;

import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class AccessBar{
    
    private final MinecraftClient client;
    private final int number;
    private final Class<?> classToAccess;
    private ArrayList<ItemStack> stacks;
    private int selectedAccessSlot = 0;
    private ItemStack lastSwappedOutTool =ItemStack.EMPTY;

    public AccessBar(int number, MinecraftClient client){
        if(number>2){
            throw new IllegalArgumentException();
        }
        this.client = client;
        this.number = number;
        this.classToAccess = WildToolAccessConfig.getClassValue("typeToAccess"+number);
    }

    public void updateAccessStacks(){
        PlayerInventory inv = client.player.inventory;

        if(!classToAccess.equals(StuffPlaceholder.class)){
            stacks = ((PlayerInventoryAccess)inv).getAllMainStacksOfType(classToAccess);
        }else{
            stacks = ((PlayerInventoryAccess)inv).getAllMainStacksWithTag(WildToolAccessConfig.stuffTag);
        }
        if(WildToolAccessConfig.getBoolValue("lastSwappedOutFirst")){
            ItemStack prioStack = inv.getStack(inv.getSlotWithStack(lastSwappedOutTool)==-1? 1000:inv.getSlotWithStack(lastSwappedOutTool));
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
        if(selectedAccessSlot!=0){
            PlayerInventory inv = client.player.inventory;
            int selectedToolPos = inv.main.indexOf(stacks.get(selectedAccessSlot-1));
            boolean putToTheRight = (WildToolAccessConfig.getBoolValue("putToTheRightIfPossible"))&&(inv.getStack(inv.selectedSlot+1) == ItemStack.EMPTY)&&!(ItemStack.areItemsEqual(inv.getStack(inv.selectedSlot), stacks.get(selectedAccessSlot-1)));
            ItemStack selectedStack = client.player.inventory.getStack(client.player.inventory.selectedSlot);
            SwapItemPacket.sendPacket(selectedToolPos, putToTheRight);

            if(this.number==1){
                client.getSoundManager().play(PositionedSoundInstance.master(WildToolAccessSoundEvents.selectInAccess1,1.0F,1.0F));

            }else{
                client.getSoundManager().play(PositionedSoundInstance.master(WildToolAccessSoundEvents.selectInAccess2,1.0F,1.0F));
            }
 
            if(classToAccess.isAssignableFrom(selectedStack.getItem().getClass())){
                lastSwappedOutTool = selectedStack.copy();
            }
        }
    }

    public void resetSelection(){
        selectedAccessSlot = 0;
    }

    public int getSelectedAccessSlot() {
        return selectedAccessSlot;
    }

    public int getNumber(){
        return this.number;
    }

    public ArrayList<ItemStack> getStacks() {
        return stacks;
    }
}
