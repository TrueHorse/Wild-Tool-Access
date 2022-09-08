package net.trueHorse.wildToolAccess;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.trueHorse.wildToolAccess.config.WildToolAccessConfig;

public class SwapItemPacket {

    private static final Identifier SWAP_PACKET_CHANNEL = new Identifier("wildtoolaccess","swap_item");

    public static void registerPacket(){
        ServerPlayNetworking.registerGlobalReceiver(SWAP_PACKET_CHANNEL, (server, player, handler, buf, sender) -> {
            int itemSlot = buf.readInt();
            boolean nextEmpty = buf.readBoolean();
            server.execute(() -> {
                if(nextEmpty&&WildToolAccessConfig.getBoolValue("putToTheRightIfPossible")){
                    ((PlayerInventoryAccess)player.getInventory()).moveSelectedAndSlot(itemSlot);
                }else{
                    ((PlayerInventoryAccess)player.getInventory()).swapSlotWithSelected(itemSlot);
                }
            });
        });
    }

    public static void sendPacket(int selectedToolPos, boolean nextEmpty){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(selectedToolPos);
        buf.writeBoolean(nextEmpty);
        ClientPlayNetworking.send(SWAP_PACKET_CHANNEL, buf);
    }
    
}
