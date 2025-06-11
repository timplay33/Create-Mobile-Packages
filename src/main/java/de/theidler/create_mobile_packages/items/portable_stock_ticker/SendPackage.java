package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.AdventureUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

public class SendPackage extends SimplePacketBase {
    private final GenericOrder order;
    private final String address;
    private final boolean encodeRequester;

    public SendPackage(GenericOrder order, String address, boolean encodeRequester) {
        this.order = order;
        this.address = address;
        this.encodeRequester = encodeRequester;
    }

    public SendPackage(FriendlyByteBuf buffer) {
        address = buffer.readUtf();
        order = GenericOrder.read(buffer);
        encodeRequester = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(address);
        order.write(buffer);
        buffer.writeBoolean(encodeRequester);
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

        ItemStack pstStack = PortableStockTicker.find(player.getInventory());
        PortableStockTicker pst = pstStack != null ? (PortableStockTicker) pstStack.getItem() : null;
        if (pst != null)
            pst.broadcastPackageRequest(
                    LogisticallyLinkedBehaviour.RequestType.PLAYER,
                    order,
                    null,
                    address,
                    player
            );
    }
}
