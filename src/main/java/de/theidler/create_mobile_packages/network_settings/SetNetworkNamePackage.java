package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class SetNetworkNamePackage implements ServerboundPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, SetNetworkNamePackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, packet -> packet.name,
            UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
            SetNetworkNamePackage::new
    );

    private final String name;
    private final UUID networkId;

    public SetNetworkNamePackage(String name, UUID networkId) {
        this.name = name;
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        LogisticsNetwork logisticsNetwork = Create.LOGISTICS.logisticsNetworks.get(networkId);
        if (logisticsNetwork == null) return;

        // validate ownership. Only the owner can change the network name
        if (player == null || logisticsNetwork.owner == null || !logisticsNetwork.owner.equals(player.getUUID()))
            return;

        // Get the extended network
        IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(logisticsNetwork);
        if (extendedNetwork == null) return;

        extendedNetwork.create_mobile_packages$setName(name);

        // Mark as dirty to persist
        Create.LOGISTICS.markDirty();

        // Send updated network data back to client
        NetworkDataPacket responsePacket = new NetworkDataPacket(
                networkId,
                logisticsNetwork.owner,
                logisticsNetwork.locked,
                extendedNetwork.create_mobile_packages$getName(),
                new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers()),
                extendedNetwork.create_mobile_packages$isOwnerMember()
        );
        CatnipServices.NETWORK.sendToClient(player, responsePacket);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.SET_NETWORK_NAME;
    }
}
