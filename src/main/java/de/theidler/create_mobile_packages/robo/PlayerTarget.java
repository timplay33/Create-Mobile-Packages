package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.toast.CustomToast;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import static de.theidler.create_mobile_packages.CMPHelper.doesAddressMatchPlayer;

public class PlayerTarget implements RoboTarget {
    private final Player player;

    public PlayerTarget(Player player) {
        this.player = player;
    }

    public static PlayerTarget fromAddress(ServerLevel level, String address) {
        ServerPlayer player = level.getPlayers((p) -> doesAddressMatchPlayer(p, address)).stream().findFirst().orElse(null);
        return new PlayerTarget(player);
    }

    @Override
    public Vec3 getTargetPos() {
        return player.position();
    }

    @Override
    public Player asPlayer() {
        return player;
    }

    @Override
    public boolean isValid() {
        return player != null && player.isAlive();
    }

    @Override
    public void setETA(VirtualRobo robo, int eta) {
        CustomToast toast = new CustomToast(
                robo.getId(),
                Component.translatable("create_mobile_packages.toast.robo_bee_on_the_way"),
                Component.translatable("create_mobile_packages.toast.eta", eta),
                CMPItems.ROBO_BEE.asStack(),
                1100
        );
        if (player instanceof ServerPlayer serverPlayer)
            CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(()-> serverPlayer), new ShowToastOnClientPacket(toast));
    }
}
