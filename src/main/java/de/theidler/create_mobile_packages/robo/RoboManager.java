package de.theidler.create_mobile_packages.robo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoboManager extends SavedData {

    public Map<UUID, VirtualRobo> robos;

    private Level level;

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

    public VirtualRobo get(UUID roboId) {
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
        this.setDirty();
    }

    public UUID newRobo(ServerLevel level, ItemStack itemStack, BlockPos spawnPos, UUID logisticsNetworkId, boolean isRequest) {
        UUID id = UUID.randomUUID();
        VirtualRobo robo = new VirtualRobo(level, id, itemStack, spawnPos, logisticsNetworkId);
        robo.setRequest(isRequest);
        this.add(robo);
        setDirty();
        return id;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private void init() {
        this.robos = new ConcurrentHashMap<>();
    }
}