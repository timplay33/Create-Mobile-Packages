package de.theidler.create_mobile_packages.robo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
}
