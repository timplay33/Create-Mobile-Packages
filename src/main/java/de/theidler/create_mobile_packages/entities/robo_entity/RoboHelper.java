package de.theidler.create_mobile_packages.entities.robo_entity;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoboHelper {
    public static @Nullable Target createTargetFromItemStack(Level level, ItemStack itemStack) {
        if (!PackageItem.isPackage(itemStack)) return null;

        Player player = getPlayerFromAddress(level, PackageItem.getAddress(itemStack));
        if (player != null) {
            return Target.createPlayerTarget(player);
        }
        return new Target(Vec3.ZERO, Target.TargetType.UNKNOWN, null, null);//TODO: fix this. values are only placeholders
    }

    public static boolean doesAddressMatchPlayerName(Player player, String address) {
        String playerName = player.getName().getString();
        int atIndex = address.lastIndexOf('@');
        if (atIndex == -1) return address.equals(playerName);
        return address.substring(atIndex + 1).equals(playerName);
    }

    public static @Nullable Player getPlayerFromAddress(Level level, String address) {
        for (Player player : level.players()) {
            if (doesAddressMatchPlayerName(player, address)) {
                return player;
            }
        }
        return null;
    }

    public static boolean isWithinBeeRange(BlockPos targetPos, BlockPos originPos) {
        int maxDistance = CMPConfigs.server().beeMaxDistance.get();
        if (targetPos == null || originPos == null) return false;
        if (maxDistance == -1) return true;
        return targetPos.distSqr(originPos) <= maxDistance * maxDistance;
    }

    /**
     * Finds the closest BeePortBlockEntity to this RoboEntity, optionally filtered by an address.
     * <p>
     * This method searches for all available BeePortBlockEntity instances in the current level.
     * If an address is provided, only ports matching the address filter are considered.
     * All full ports are removed from the selection.
     * Finally, the closest port to this RoboEntity's position is determined.
     *
     * @param address The address to filter by, or {@code null} for no filtering.
     * @return The closest BeePortBlockEntity that matches the filter criteria, or {@code null} if none found.
     */
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, BlockPos origin, RoboEntity entity) {
        List<BeePortBlockEntity> allBEs = getAllBeePortsByAddress(level, address);

        allBEs.removeIf(dpbe -> !isWithinBeeRange(dpbe.getBlockPos(), origin));

        allBEs.removeIf(dpbe -> !dpbe.canAcceptEntity(entity, (entity != null && !entity.getItemStack().isEmpty())));

        return allBEs.stream()
                .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(origin)))
                .orElse(null);
    }

    public static ArrayList<BeePortBlockEntity> getAllBeePorts(Level level) {
        ArrayList<BeePortBlockEntity> allBEs = new ArrayList<>();
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> allBEs.addAll(tracker.getAll()));
        allBEs.removeIf(BlockEntity::isRemoved);
        return allBEs;
    }

    public static ArrayList<BeePortBlockEntity> getAllBeePortsByAddress(Level level, String address) {
        ArrayList<BeePortBlockEntity> allBEs = getAllBeePorts(level);
        if (address != null) {
            allBEs.removeIf(dpbe -> !PackageItem.matchAddress(address, dpbe.addressFilter));
        }
        return allBEs;
    }
}
