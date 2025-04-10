package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import static de.theidler.create_mobile_packages.items.drone_controller.StockCheckingItem.getAccurateSummary;

public class RequestStockUpdate extends SimplePacketBase {

    public RequestStockUpdate() {
        CreateMobilePackages.LOGGER.info("Constructed new RequestStockUpdate Packet");
    }
    public RequestStockUpdate(FriendlyByteBuf buffer) {
        CreateMobilePackages.LOGGER.info("Constructed new RequestStockUpdate Packet from Buffer");
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        CreateMobilePackages.LOGGER.info("Writing RequestStockUpdate Packet");
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        CreateMobilePackages.LOGGER.info("RequestStockUpdate Packet received by handle on {}", context.getDirection());
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {

                BigItemStackListPacket responsePacket = new BigItemStackListPacket(getAccurateSummary(player.getMainHandItem()).getStacks());
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
            }
        });
        return true;
    }
}
