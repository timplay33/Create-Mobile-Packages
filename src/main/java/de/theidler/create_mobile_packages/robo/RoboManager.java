package de.theidler.create_mobile_packages.robo;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.DronePortTracker;
import de.theidler.create_mobile_packages.blocks.bee_port.RoboRequest;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.SyncTrashItemsToClientPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
    public List<RoboTrashStore> roboTrashStores;

    public RoboManager() {
        init();
    }

    public static RoboManager load(CompoundTag tag, ServerLevel level) {
        RoboManager manager = new RoboManager();

        // Load robos
        ListTag robosList = tag.getList("robos", Tag.TAG_COMPOUND);
        for (int i = 0; i < robosList.size(); i++) {
            CompoundTag roboTag = robosList.getCompound(i);
            VirtualRobo robo = VirtualRobo.deserializeNBT(level, roboTag);
            manager.robos.put(robo.getId(), robo);
        }

        // Load Trash Stores
        ListTag trashSlotsTag = tag.getList("trashSlots", Tag.TAG_COMPOUND);
        for (int i = 0; i < trashSlotsTag.size(); i++) {
            CompoundTag trashStoreTag = trashSlotsTag.getCompound(i);
            RoboTrashStore roboTrashStore = RoboTrashStore.load(trashStoreTag);
            manager.roboTrashStores.add(roboTrashStore);
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag robosList = new ListTag();
        for (VirtualRobo robo : robos.values()) {
            robosList.add(robo.serializeNBT());
        }
        tag.put("robos", robosList);

        // Save Trash Stores
        ListTag trashSlotsTag = new ListTag();
        for (RoboTrashStore roboTrashStore : roboTrashStores) {
            trashSlotsTag.add(roboTrashStore.save());
        }
        tag.put("trashSlots", trashSlotsTag);
        return tag;
    }

    public static RoboManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(RoboManager::new, (tag, provider) -> RoboManager.load(tag, level)),
            "create_mobile_packages_robo_manager"
        );
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

    public @Nullable RoboTrashStore getTrashStore(UUID networkId, UUID playerId) {
        return roboTrashStores.stream()
                .filter((store) -> store.getNetworkUUID().equals(networkId))
                .filter((store) -> store.getPlayerUUID().equals(playerId))
                .findFirst().orElse(null);
    }

    public synchronized void setTrashSlots(UUID networkId, UUID playerId, List<ItemStack> trashSlots) {
        RoboTrashStore existingStore = getTrashStore(networkId, playerId);
        if (existingStore != null) {
            List<ItemStack> storeItems = existingStore.getItemStacks();
            storeItems.clear();
            // Create copies of the ItemStacks to ensure proper data transfer
            for (ItemStack stack : trashSlots) {
                storeItems.add(stack.copy());
            }
        } else {
            // Create a new mutable list with copies of the stacks
            List<ItemStack> copiedStacks = new ArrayList<>();
            for (ItemStack stack : trashSlots) {
                copiedStacks.add(stack.copy());
            }
            roboTrashStores.add(new RoboTrashStore(playerId, networkId, copiedStacks));
        }
        this.setDirty();
    }

    public synchronized void setTrashSlots(ServerLevel level, UUID networkId, UUID playerId, List<ItemStack> trashSlots) {
        setTrashSlots(networkId, playerId, trashSlots);

        // Send sync packet to the player
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) {
            CatnipServices.NETWORK.sendToClient(player, new SyncTrashItemsToClientPacket(new ArrayList<>(trashSlots)));
        }
    }

    public synchronized void setTrashTargetAddress(UUID networkId, UUID playerId, String targetAddress) {
        RoboTrashStore existingStore = getTrashStore(networkId, playerId);
        if (existingStore != null) {
            existingStore.setTargetAddress(targetAddress != null ? targetAddress : "");
        } else {
            List<ItemStack> emptyStacks = new ArrayList<>();
            RoboTrashStore store = new RoboTrashStore(playerId, networkId, emptyStacks);
            store.setTargetAddress(targetAddress != null ? targetAddress : "");
            roboTrashStores.add(store);
        }
        this.setDirty();
    }

    public void tick(ServerLevel level) {
        robos.values().forEach(robo -> robo.tick(level));
        getPendingRoboRequests().forEach(roboRequest -> tryHandlingRequest(roboRequest, level));
        // prune finished requests older than a minute to avoid unbounded growth
        long now = System.currentTimeMillis();
        beePortRoboRequests.removeIf(r -> (r.getStatus() == RoboRequest.Status.DONE || r.getStatus() == RoboRequest.Status.CANCELLED) && (now - r.getCreatedAt()) > 60_000);

        // handle trash stores
        roboTrashStores.forEach(store -> {
            if (store.getItemStacks().isEmpty()) return;
            Player player = level.getPlayerByUUID(store.getPlayerUUID());
            if (player == null) return;

            UUID networkId = store.getNetworkUUID();
            UUID playerId = store.getPlayerUUID();

            // Check if there is already an active request for this player and network
            // This will also clean up dead robos by marking their requests as CANCELLED
            if (hasActiveTrashRequest(playerId, networkId)) {
                return;
            }

            // At this point, there's no active request, so we can create a new one
            // (The previous request either completed, was cancelled, or had a dead robo)
            RoboRequest request = new RoboRequest(new PlayerTarget(
                    player,
                    networkId
            ), networkId, RoboRequest.Mission.PICKUP);

            store.setLastRequest(request);
            requestRobo(request);
        });

        this.setDirty();
    }

    private void tryHandlingRequest(RoboRequest request, ServerLevel level) {
        DronePortTracker tracker = DronePortTracker.get(level);
        List<BeePortBlockEntity> allBEs = new ArrayList<>(tracker.getAllByNetwork(request.getLogisticsNetworkId()));
        allBEs.removeIf(BlockEntity::isRemoved);
        allBEs.removeIf(be -> be.getBlockPos().equals(BlockPos.containing(request.getTargetPos())));
        allBEs.removeIf(be -> be.getRoboBeeInventory().getStackInSlot(0).getCount() <= 0);
        allBEs.stream().min(Comparator.comparingDouble(a -> a.getBlockPos().getCenter().distanceToSqr(request.getTargetPos()))).ifPresent(target -> target.handleRequest(request));
    }

    /**
     * Checks if there is already an active request (PENDING or IN_PROGRESS) for the given player and network.
     * This ensures only one bee is sent per player+network combination at a time.
     * If a request is IN_PROGRESS but the robo is dead, it will be cancelled and return false.
     */
    private boolean hasActiveTrashRequest(UUID playerId, UUID networkId) {
        for (RoboRequest request : beePortRoboRequests) {
            if (!request.getLogisticsNetworkId().equals(networkId)) continue;
            if (!(request.getTarget() instanceof PlayerTarget target)) continue;

            if (target.asPlayer() == null || !target.asPlayer().getUUID().equals(playerId)) continue;

            // Check if this is an active request
            if (request.getStatus() == RoboRequest.Status.PENDING) {
                // For PENDING requests, check if there's a robo assigned to it
                boolean roboExists = robos.values().stream()
                        .anyMatch(robo -> robo.getRequest() == request);
                if (roboExists) {
                    return true;
                }
                // If no robo exists for this PENDING request, let it be handled again
            }

            if (request.getStatus() == RoboRequest.Status.IN_PROGRESS) {
                // Check if the robo actually exists for this request
                boolean roboExists = robos.values().stream()
                        .anyMatch(robo -> robo.getRequest() == request);

                if (roboExists) {
                    return true;
                } else {
                    // Robo is dead but request is still IN_PROGRESS - mark as cancelled
                    request.setStatus(RoboRequest.Status.CANCELLED);
                }
            }
        }
        return false;
    }

    public UUID newRobo(ServerLevel level, ItemStack itemStack, BlockPos spawnPos, UUID logisticsNetworkId, float packageHeightScale, @Nullable BlockPos homePort) {
        UUID id = UUID.randomUUID();
        VirtualRobo robo = new VirtualRobo(level, id, itemStack, spawnPos, logisticsNetworkId);
        robo.setPackageHeightScale(packageHeightScale);
        robo.setHomePortPos(homePort);
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

    public void requestRobo(RoboTarget roboTarget, UUID logisticsNetworkId, RoboRequest.Mission mission) {
        requestRobo(new RoboRequest(roboTarget, logisticsNetworkId, mission));
    }

    public synchronized void requestRobo(RoboRequest request) {
        beePortRoboRequests.add(request);
        setDirty();
    }

    public List<RoboRequest> getRoboRequestsWithStatus(RoboRequest.Status status) {
        return beePortRoboRequests.stream().filter(request -> request.getStatus() == status).toList();
    }

    public List<RoboRequest> getPendingRoboRequests() {
        return getRoboRequestsWithStatus(RoboRequest.Status.PENDING);
    }

    public List<RoboRequest> getRoboRequests(BlockPos pos) {
        return beePortRoboRequests.stream().filter(request -> BlockPos.containing(request.getTargetPos()).equals(pos)).toList();
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
        this.roboTrashStores = new ArrayList<>();
    }
}

