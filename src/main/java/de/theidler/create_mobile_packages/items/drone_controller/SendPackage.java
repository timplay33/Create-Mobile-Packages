package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.AdventureUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class SendPackage extends SimplePacketBase {
    private final PackageOrder order;
    private final String address;
    private final boolean encodeRequester;
    private final PackageOrder craftingRequest;

    public SendPackage(PackageOrder order, String address, boolean encodeRequester, PackageOrder craftingRequest) {
        this.order = order;
        this.address = address;
        this.encodeRequester = encodeRequester;
        this.craftingRequest = craftingRequest;
    }

    public SendPackage(FriendlyByteBuf buffer) {
        address = buffer.readUtf();
        order = PackageOrder.read(buffer);
        encodeRequester = buffer.readBoolean();
        craftingRequest = PackageOrder.read(buffer);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(address);
        order.write(buffer);
        buffer.writeBoolean(encodeRequester);
        craftingRequest.write(buffer);
    }


    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
                return;
            Level world = player.level();
            if (!world.isLoaded(player.blockPosition()))
                return;
            applySettings(player);
        });
        return true;
    }

    protected void applySettings(ServerPlayer player) {

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(player.level(), player.blockPosition());
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), player.blockPosition());
        }

        if (player.getMainHandItem().getItem() instanceof DroneController) {
            ((DroneController) player.getMainHandItem().getItem()).broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, address);
        }
    }
}
