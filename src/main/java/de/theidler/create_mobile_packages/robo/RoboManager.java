package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.ModCapabilities;
import de.theidler.create_mobile_packages.blocks.bee_port.RoboRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoboManager extends SavedData {

    public Map<UUID, VirtualRobo> robos;
    public List<RoboRequest> beePortRoboRequests;

    public RoboManager() {
        init();
    }

    public static RoboManager load(ServerLevel level, CompoundTag tag) {
        RoboManager manager = new RoboManager();

        // Load robos
        ListTag robosList = tag.getList("robos", Tag.TAG_COMPOUND);
        for (int i = 0; i < robosList.size(); i++) {
            CompoundTag roboTag = robosList.getCompound(i);
            VirtualRobo robo = VirtualRobo.deserializeNBT(level, roboTag);
            manager.robos.put(robo.getId(), robo);
        }
        return manager;
    }

    public static RoboManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent((tag) -> RoboManager.load(level, tag), RoboManager::new, "create_mobile_packages_robo_manager");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag robosList = new ListTag();
        for (VirtualRobo robo : robos.values()) {
            robosList.add(robo.serializeNBT());
        }
        tag.put("robos", robosList);
        return tag;
    }

    public @Nullable VirtualRobo get(UUID roboId) {
        return robos.get(roboId);
    }

    public void remove(UUID roboId) {
        robos.remove(roboId);
        this.setDirty();
    }

    public void add(VirtualRobo robo) {
        robos.put(robo.getId(), robo);
        this.setDirty();
    }

    public void tick(ServerLevel level) {
        robos.values().forEach(robo -> robo.tick(level));
        getPendingRoboRequests().forEach(roboRequest -> tryHandlingRequest(roboRequest, level));
        // prune finished requests older than a minute to avoid unbounded growth
        long now = System.currentTimeMillis();
        beePortRoboRequests.removeIf(r -> (r.getStatus() == RoboRequest.Status.DONE || r.getStatus() == RoboRequest.Status.CANCELLED) && (now - r.getCreatedAt()) > 60_000);
        this.setDirty();
    }

    private void tryHandlingRequest(RoboRequest request, ServerLevel level) {
        level.getCapability(ModCapabilities.BEE_PORT_ENTITY_TRACKER_CAP).ifPresent(tracker -> {
            List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAllByNetwork(request.getLogisticsNetworkId()));
            allBEs.removeIf(BlockEntity::isRemoved);
            allBEs.removeIf(be -> be.getBlockPos().equals(request.getTargetPos()));
            allBEs.removeIf(be -> be.getRoboBeeInventory().getStackInSlot(0).getCount() <= 0);
            allBEs.stream().min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(request.getTargetPos()))).ifPresent(target -> target.handleRequest(request));
        });
    }

    public UUID newRobo(ServerLevel level, ItemStack itemStack, BlockPos spawnPos, UUID logisticsNetworkId, float packageHeightScale, @Nullable BlockPos HomePort) {
        UUID id = UUID.randomUUID();
        VirtualRobo robo = new VirtualRobo(level, id, itemStack, spawnPos, logisticsNetworkId);
        robo.setPackageHeightScale(packageHeightScale);
        robo.setHomePortPos(HomePort);
        this.add(robo);
        setDirty();
        return id;
    }

    public void newRequestRobo(ServerLevel level, BlockPos spawnPos, RoboRequest request) {
        UUID id = UUID.randomUUID();
        VirtualRobo robo = new VirtualRobo(level, id, ItemStack.EMPTY, spawnPos, request.getLogisticsNetworkId());
        robo.setRequest(request);
        this.add(robo);
        setDirty();
    }

    public synchronized void requestRobo(BlockPos pos, UUID logisticsNetworkId) {
        beePortRoboRequests.add(new RoboRequest(pos, logisticsNetworkId));
    }

    public List<RoboRequest> getRoboRequestsWithStatus(RoboRequest.Status status) {
        return beePortRoboRequests.stream().filter(request -> request.getStatus() == status).toList();
    }

    public List<RoboRequest> getPendingRoboRequests() {
        return getRoboRequestsWithStatus(RoboRequest.Status.PENDING);
    }

    public List<RoboRequest> getRoboRequests(BlockPos pos) {
        return beePortRoboRequests.stream().filter(request -> request.getTargetPos().equals(pos)).toList();
    }

    public List<VirtualRobo> getInboundRobo(BlockPos pos) {
        List<VirtualRobo> inboundRobos = new ArrayList<>();
        for (VirtualRobo robo : robos.values()) {
            RoboTarget target = robo.getTarget();
            if (target != null && target.getTargetPos() != null && BlockPos.containing(target.getTargetPos()).equals(pos)) {
                inboundRobos.add(robo);
            }
        }
        return inboundRobos;
    }

    public List<Integer> getETAs(BlockPos pos) {
        List<Integer> eta = new ArrayList<>();
        // add eta for 2 sources
        getRoboRequests(pos).stream().map(RoboRequest::getEta).forEach(eta::add);
        getInboundRobo(pos).stream().map(robo -> {
            RoboTarget target = robo.getTarget();
            if (target != null) return target.getETA();
            return -1;
        }).forEach(eta::add);
        return eta.stream().filter(integer -> integer >= 0).toList();
    }

    private void init() {
        this.robos = new ConcurrentHashMap<>();
        this.beePortRoboRequests = new CopyOnWriteArrayList<>();
    }
}