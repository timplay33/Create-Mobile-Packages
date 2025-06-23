package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.AdventureUtil;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

public class SendPackage implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SendPackage> STREAM_CODEC = StreamCodec.composite(
            GenericOrder.STREAM_CODEC, packet -> packet.order,
            ByteBufCodecs.STRING_UTF8, packet -> packet.address,
            SendPackage::new
    );

    private final GenericOrder order;
    private final String address;

    public SendPackage(GenericOrder order, String address, boolean encodeRequester) {
        this.order = order;
        this.address = address;
    }

    protected void applySettings(ServerPlayer player) {

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(player.level(), player.blockPosition());
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), player.blockPosition());
        }

        ItemStack pstStack = PortableStockTicker.find(player.getInventory());
        PortableStockTicker pst = pstStack != null ? (PortableStockTicker) pstStack.getItem() : null;
        if (pstStack != null) {
            pst.broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, address, player);
        }
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
            return;
        Level world = player.level();
        if (!world.isLoaded(player.blockPosition()))
            return;
        applySettings(player);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.LOGISTICS_PACKAGE_REQUEST;
    }
}
