package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestNetworkDataPacket extends SimplePacketBase {

    private final UUID networkId;

    public RequestNetworkDataPacket(UUID networkId) {
        this.networkId = networkId;
    }

    public RequestNetworkDataPacket(FriendlyByteBuf buffer) {
        this.networkId = buffer.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(networkId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                CreateMobilePackages.LOGGER.warn("RequestNetworkDataPacket: player is null");
                return;
            }

            LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(networkId);
            if (network == null) {
                CreateMobilePackages.LOGGER.warn("RequestNetworkDataPacket: network {} not found", networkId);
                // Send error packet
                NetworkDataPacket errorPacket = new NetworkDataPacket(networkId, null, false, "ERROR: Network not found", new ArrayList<>());
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), errorPacket);
                return;
            }

            CreateMobilePackages.LOGGER.debug("RequestNetworkDataPacket: Sending network data for {} to player {}", networkId, player.getName().getString());

            // Extract data directly from network NBT to avoid mixin issues
            String name = "Logistics Network " + networkId.toString().substring(0, 4);
            List<UUID> players = new ArrayList<>();

            try {
                // Try to get extended network data
                IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(network);
                if (extendedNetwork != null) {
                    name = extendedNetwork.create_mobile_packages$getName();
                    players = new ArrayList<>(extendedNetwork.create_mobile_packages$getPlayers());
                    CreateMobilePackages.LOGGER.debug("RequestNetworkDataPacket: Got data from extended network - name: '{}', {} players", name, players.size());
                } else {
                    // Fallback: try to read from NBT
                    CreateMobilePackages.LOGGER.debug("RequestNetworkDataPacket: extendedNetwork is null, trying NBT fallback");
                    CompoundTag tag = network.write();
                    if (tag.contains("name", Tag.TAG_STRING)) {
                        name = tag.getString("name");
                    }
                    if (tag.contains("CMP_Players", Tag.TAG_LIST)) {
                        final List<UUID> playerList = players;
                        NBTHelper.iterateCompoundList(
                                tag.getList("CMP_Players", Tag.TAG_COMPOUND),
                                nbt -> playerList.add(nbt.getUUID("UUID"))
                        );
                    }
                    CreateMobilePackages.LOGGER.debug("RequestNetworkDataPacket: Got data from NBT - name: '{}', {} players", name, players.size());
                }
            } catch (Exception e) {
                CreateMobilePackages.LOGGER.warn("RequestNetworkDataPacket: Error extracting network data", e);
                NetworkDataPacket errorPacket = new NetworkDataPacket(networkId, null, false, "ERROR: " + e.getMessage(), new ArrayList<>());
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), errorPacket);
                return;
            }

            // Send network data back to client
            NetworkDataPacket responsePacket = new NetworkDataPacket(
                    networkId,
                    network.owner,
                    network.locked,
                    name,
                    players
            );
            CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
        });
        return true;
    }
}
