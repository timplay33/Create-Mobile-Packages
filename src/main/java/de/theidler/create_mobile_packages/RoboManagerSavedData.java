package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoboManagerSavedData extends SavedData {

    private Map<UUID, RoboEntity> robos = new HashMap<>();

    public static RoboManagerSavedData load(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(RoboManagerSavedData::load, RoboManagerSavedData::new, "robo_manager");
    }

    public static RoboManagerSavedData load(CompoundTag nbt) {
        RoboManagerSavedData savedData = new RoboManagerSavedData();
        savedData.robos = new HashMap<>();
        Level level = CreateMobilePackages.ROBO_MANAGER.getLevel();
        NBTHelper.iterateCompoundList(nbt.getList("Robos", CompoundTag.TAG_COMPOUND), c -> {
            if (level.getEntity(c.getInt("Id")) instanceof RoboEntity robo)
                savedData.robos.put(robo.getUUID(), robo);
        });
        return savedData;
    }

    public Map<UUID, RoboEntity> getRobos() {
        return robos;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        RoboManager roboManager = CreateMobilePackages.ROBO_MANAGER;
        CreateMobilePackages.LOGGER.info("Saving RoboManager...");
        nbt.put("Robos", NBTHelper.writeCompoundList(roboManager.robos.values(), robo -> {
            CompoundTag roboTag = new CompoundTag();
            roboTag.putInt("Id", robo.getId());
            return roboTag;
        }));
        return nbt;
    }
}
