package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModifyNetworkLockStatePackage implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ModifyNetworkLockStatePackage> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.BOOL, packet -> packet.locked,
                    UUIDUtil.STREAM_CODEC, packet -> packet.networkId,
                    ModifyNetworkLockStatePackage::new
            );

    private final boolean locked;
    private final UUID networkId;

    public ModifyNetworkLockStatePackage(boolean locked, UUID networkId) {
        this.locked = locked;
        this.networkId = networkId;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        if (network == null) return;

        // Only the owner can lock/unlock the network
        if (!player.getUUID().equals(network.owner)) return;

        network.locked = locked;
        Create.LOGISTICS.markDirty();

        // Send updated network data back to client
        IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(network);
        if (extendedNetwork != null) {
            List<UUID> updatedPlayers = new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers());
            Map<UUID, String> playerNames = NetworkHelper.buildNetworkPlayerNames(player.serverLevel(), updatedPlayers, network.owner);
            NetworkDataPacket responsePacket = new NetworkDataPacket(
                    networkId,
                    network.owner,
                    network.locked,
                    extendedNetwork.create_mobile_packages$getName(),
                    updatedPlayers,
                    extendedNetwork.create_mobile_packages$isOwnerMember(),
                    playerNames
            );
            CatnipServices.NETWORK.sendToClient(player, responsePacket);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.MODIFY_NETWORK_LOCK_STATE;
    }
}
