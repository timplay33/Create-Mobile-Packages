package de.theidler.create_mobile_packages.blocks.portal_port;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PortalPortTracker extends SavedData {
    private final Set<BlockPos> portalPorts = new HashSet<>();

    public static PortalPortTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(PortalPortTracker::create, PortalPortTracker::load), "portal_port_tracker");
    }

    public static PortalPortTracker create() {
        return new PortalPortTracker();
    }

    public static PortalPortTracker load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        PortalPortTracker tracker = create();
        ListTag positions = tag.getList("positions", Tag.TAG_COMPOUND);
        for (Tag entry : positions) {
            if (entry instanceof CompoundTag posTag) {
                tracker.portalPorts.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
        }
        return tracker;
    }

    public void add(BlockPos portalPortPos) {
        if (portalPorts.add(portalPortPos.immutable())) {
            setDirty();
        }
    }

    public void remove(BlockPos portalPortPos) {
        if (portalPorts.remove(portalPortPos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAll() {
        return Set.copyOf(portalPorts);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        ListTag positions = new ListTag();
        for (BlockPos pos : portalPorts) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            positions.add(posTag);
        }
        compoundTag.put("positions", positions);
        return compoundTag;
    }
}

