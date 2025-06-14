package de.theidler.create_mobile_packages.entities.robo_bee_entity;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Helpers {

    public static String getPlayerNameByAddress(String address) {
        int atIndex = address.indexOf('@');
        if (atIndex == -1) {
            return address;
        }
        return address.substring(0, atIndex);
    }

    public static @Nullable Player getPlayerByName(Level level, String playerName) {
        return level.players().stream().filter(player -> player.getName().getString().equals(playerName)).findFirst().orElse(null);
    }

    public static @Nullable BeePortBlockEntity getClosestBeePort(Level level, RoboBeeEntity roboBee) {
        List<BeePortBlockEntity> allBEs = new ArrayList<>();
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> allBEs.addAll(tracker.getAll()));

        return allBEs.stream()
                .filter(beePort -> beePort.canAcceptEntity(roboBee, !roboBee.getItemStack().isEmpty()))
                .min(Comparator.comparingDouble(beePort -> beePort.getBlockPos().getCenter().distanceToSqr(roboBee.position())))
                .orElse(null);
    }

    public static void flyToVec3(Mob mob, Vec3 vec3) {
        double distance = mob.position().distanceTo(vec3);
        if (distance > 2.0) {
            mob.getNavigation().moveTo(
                    vec3.x(),
                    vec3.y() + 2f,
                    vec3.z(),
                    1.0f
            );
        } else {
            mob.getNavigation().stop();
        }
    }
}
