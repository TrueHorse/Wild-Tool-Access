package net.trueHooorse.wildToolAccess;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.trueHooorse.wildToolAccess.config.WildToolAccessConfig;

public class SwapItemPacket {

    private static final Identifier SWAP_PACKET_CHANNEL = new Identifier("wildtoolaccess","swap_item");

    public static void registerPacket(){
        ServerPlayNetworking.registerGlobalReceiver(SWAP_PACKET_CHANNEL, (server, player, handler, buf, sender) -> {
            int itemSlot = buf.readInt();
            Boolean nextEmpty = buf.readBoolean();
            server.execute(() -> {
                if(nextEmpty&&WildToolAccessConfig.getBoolValue("moveIfNextEmpty")){
                    ((PlayerInventoryAccess)player.inventory).moveSelectedAndSlot(itemSlot);
                }else{
                    ((PlayerInventoryAccess)player.inventory).swapSlotWithSelected(itemSlot);
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
