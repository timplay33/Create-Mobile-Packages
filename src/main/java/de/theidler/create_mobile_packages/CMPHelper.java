package de.theidler.create_mobile_packages;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.DronePortTracker;
import de.theidler.create_mobile_packages.blocks.portal_port.PortalPortTracker;
import de.theidler.create_mobile_packages.compat.sable.SableCompat;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.robo.BeePortBlockEntityTarget;
import de.theidler.create_mobile_packages.robo.PlayerTarget;
import de.theidler.create_mobile_packages.robo.RoboTarget;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CMPHelper {

    public static @Nullable PortalTransferRoute findPortalTransferRoute(ServerLevel sourceLevel, String address, BlockPos origin, VirtualRobo robo, UUID logisticsNetworkId) {
        Set<BlockPos> sourcePortals = getPortalPortPositions(sourceLevel);
        if (sourcePortals.isEmpty()) {
            return null;
        }

        BlockPos sourcePortal = sourcePortals.stream()
                .min(Comparator.comparingDouble(pos -> Vec3.atCenterOf(pos).distanceToSqr(Vec3.atCenterOf(origin))))
                .orElse(null);
        if (sourcePortal == null) {
            return null;
        }

        CreateMobilePackages.LOGGER.debug("findPortalTransferRoute: source level {} has {} portals, nearest={}",
                sourceLevel.dimension().location(), sourcePortals.size(), sourcePortal);

        PortalTransferRoute bestRoute = null;
        double bestDistance = Double.MAX_VALUE;

        for (ServerLevel candidateLevel : sourceLevel.getServer().getAllLevels()) {
            if (candidateLevel == sourceLevel) {
                continue;
            }
            CreateMobilePackages.LOGGER.debug("  checking candidate level: {}", candidateLevel.dimension().location());

            Set<BlockPos> destinationPortals = getPortalPortPositions(candidateLevel);
            if (destinationPortals.isEmpty()) {
                CreateMobilePackages.LOGGER.debug("    no portals in target level");
                continue;
            }
            CreateMobilePackages.LOGGER.debug("    found {} portals in target level", destinationPortals.size());

            RoboTarget finalTarget = findCrossLevelTarget(candidateLevel, address, robo, logisticsNetworkId);
            if (finalTarget == null || finalTarget.getTargetPos() == null || !finalTarget.isValid(robo)) {
                CreateMobilePackages.LOGGER.debug("    no valid target found in this level (address='{}')", address);
                continue;
            }
            CreateMobilePackages.LOGGER.debug("    found valid target at {}", finalTarget.getTargetPos());

            Vec3 finalTargetPos = finalTarget.getTargetPos();
            BlockPos destinationPortal = destinationPortals.stream()
                    .min(Comparator.comparingDouble(pos -> Vec3.atCenterOf(pos).distanceToSqr(finalTargetPos)))
                    .orElse(null);

            if (destinationPortal == null) {
                CreateMobilePackages.LOGGER.debug("    failed to find destination portal");
                continue;
            }

            double destinationDistance = Vec3.atCenterOf(destinationPortal).distanceToSqr(finalTargetPos);
            if (destinationDistance < bestDistance) {
                bestDistance = destinationDistance;
                bestRoute = new PortalTransferRoute(sourcePortal.immutable(), candidateLevel, destinationPortal.immutable(), finalTarget);
                CreateMobilePackages.LOGGER.debug("    new best route found: {} -> portal {} (distance={})", candidateLevel.dimension().location(), destinationPortal, destinationDistance);
            }
        }

        if (bestRoute == null) {
            CreateMobilePackages.LOGGER.debug("findPortalTransferRoute: no valid route found");
        }
        return bestRoute;
    }

    public static Vec3 readVec3FromTag(CompoundTag tag, String key) {
        double x = tag.getDouble(key + "X");
        double y = tag.getDouble(key + "Y");
        double z = tag.getDouble(key + "Z");
        return new Vec3(x, y, z);
    }

    public static void writeVec3ToTag(CompoundTag tag, String key, Vec3 vec) {
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
    }

    public static boolean isWithinRange(Level level, BlockPos targetPos, BlockPos originPos) {
        int maxDistance = CMPConfigs.server().beeMaxDistance.get();
        if (targetPos == null || originPos == null) return false;
        if (maxDistance == -1) return true;
        Vec3 projectedTarget = projectOutOfSubLevel(level, Vec3.atCenterOf(targetPos));
        Vec3 projectedOrigin = projectOutOfSubLevel(level, Vec3.atCenterOf(originPos));
        return projectedTarget.distanceToSqr(projectedOrigin) <= maxDistance * maxDistance;
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
    public static BeePortBlockEntity getClosestBeePort(Level level, String address, BlockPos origin, VirtualRobo entity, UUID logisticsNetworkId) {
        if (level instanceof ServerLevel serverLevel) {
            DronePortTracker tracker = DronePortTracker.get(serverLevel);
            List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAllByNetwork(logisticsNetworkId));
            if (allBEs.isEmpty()) {
                // if there are no Bee Ports in the network, then allow the bee to fly to any network
                allBEs.addAll(tracker.getAll());
            }
            allBEs.removeIf(BlockEntity::isRemoved);
            allBEs.removeIf(dpbe -> !isWithinRange(level, dpbe.getBlockPos(), origin));
            if (address != null && !address.isEmpty()) {
                allBEs.removeIf(dpbe -> !PackageItem.matchAddress(address, dpbe.addressFilter));
            }
            allBEs.removeIf(dpbe -> !dpbe.canAcceptEntity(entity, (entity != null && !entity.getItemStack().isEmpty())));
            Vec3 projectedOrigin = projectOutOfSubLevel(level, Vec3.atCenterOf(origin));
            return allBEs.stream()
                    .min(Comparator.comparingDouble(a -> getGlobalCenter(level, a.getBlockPos()).distanceToSqr(projectedOrigin)))
                    .orElse(null);
        }
        return null;
    }

    /**
     * Calculates the estimated time of arrival (ETA) to the specified targetPosition.
     *
     * @param targetPosition The Vec3 to calculate the ETA for.
     * @return The ETA in seconds.
     */
    public static int calcETA(Vec3 targetPosition, Vec3 currentPosition) {
        if (targetPosition == null || currentPosition == null) return -1;
        double distance = targetPosition.distanceTo(currentPosition);
        return (int) (distance / CMPConfigs.server().beeSpeed.get()) + 1;
    }

    public static boolean doesAddressMatchPlayer(Player player, String address) {
        if (address == null) return false;
        String playerName = player.getName().getString();
        int atIndex = address.lastIndexOf('@');
        if (atIndex == -1) {
            return address.equals(playerName);
        }
        return address.substring(atIndex + 1).equals(playerName);
    }

    public static @Nullable BeePortBlockEntity getPortAtPos(ServerLevel serverLevel, @Nullable BlockPos pos) {
        if (pos == null) return null;
        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be instanceof BeePortBlockEntity bpbe) {
            return bpbe;
        }
        return null;
    }

    public static Set<BlockPos> getPortalPortPositions(ServerLevel serverLevel) {
        return PortalPortTracker.get(serverLevel).getAll();
    }

    private static @Nullable RoboTarget findCrossLevelTarget(ServerLevel level, String address, VirtualRobo robo, UUID logisticsNetworkId) {
        PlayerTarget playerTarget = PlayerTarget.fromAddress(level, address, logisticsNetworkId);
        if (playerTarget != null) {
            CreateMobilePackages.LOGGER.debug("      found player target: {}", address);
            return playerTarget;
        }

        BeePortBlockEntity addressedPort = CMPHelper.getClosestBeePort(level, address, BlockPos.containing(robo.getCurrentPos()), robo, logisticsNetworkId);
        if (addressedPort != null) {
            CreateMobilePackages.LOGGER.debug("      found addressed port: {} at {}", address, addressedPort.getBlockPos());
            return new BeePortBlockEntityTarget(addressedPort);
        }

        return null;
    }

    public record PortalTransferRoute(BlockPos sourcePortalPos, ServerLevel destinationLevel,
                                      BlockPos destinationPortalPos, RoboTarget finalTarget) {
    }

    public static Vec3 getGlobalCenter(@Nullable Level level, BlockPos pos) {
        return projectOutOfSubLevel(level, Vec3.atCenterOf(pos));
    }

    public static Vec3 projectOutOfSubLevel(@Nullable Level level, Vec3 pos) {
        if (level == null || pos == null) {
            return pos;
        }
        return SableCompat.projectOutOfSubLevel(level, pos);
    }
}
