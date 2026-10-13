package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.network_settings.NetworkHelper;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ToggleFilterModePacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleFilterModePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.portPos,
            ToggleFilterModePacket::new
    );

    private final BlockPos portPos;

    public ToggleFilterModePacket(BlockPos portPos) {
        this.portPos = portPos;
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;
        ServerLevel serverLevel = player.serverLevel();
        if (!(serverLevel.getBlockEntity(portPos) instanceof BeePortBlockEntity beePort)) return;

        if (player.distanceToSqr(portPos.getCenter()) > 64.0) return;

        if (beePort.behaviour != null) {
            IExtendedLogisticsNetwork network = NetworkHelper.getExtendedLogisticsNetwork(beePort.behaviour.freqId);
            if (network != null
                    && !network.create_mobile_packages$getPlayers().contains(player.getUUID())
                    && !beePort.behaviour.mayInteractMessage(player)
            ) {
                return;
            }
        }

        beePort.toggleFilterMode();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CMPPackets.TOGGLE_FILTER_MODE;
    }
}
