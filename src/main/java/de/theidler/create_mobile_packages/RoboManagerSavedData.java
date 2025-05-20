package de.theidler.create_mobile_packages;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoboManagerSavedData extends SavedData {

    private Map<UUID, SimpleRobo> robos = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag nbt) {
        RoboManager roboManager = CreateMobilePackages.ROBO_MANAGER;
        CreateMobilePackages.LOGGER.info("Saving RoboManager...");
        DimensionPalette dimensions = new DimensionPalette();
        nbt.put("Robos", NBTHelper.writeCompoundList(roboManager.robos.values(), robo -> robo.write(dimensions)));
        dimensions.write(nbt);
        return nbt;
    }

    public static RoboManagerSavedData load(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(RoboManagerSavedData::load, RoboManagerSavedData::new, "robo_manager");
    }

    public static RoboManagerSavedData load(CompoundTag nbt) {
        RoboManagerSavedData savedData = new RoboManagerSavedData();
        savedData.robos = new HashMap<>();

        DimensionPalette dimensions = DimensionPalette.read(nbt);
        NBTHelper.iterateCompoundList(nbt.getList("Robos", CompoundTag.TAG_COMPOUND), c -> {
            SimpleRobo simpleRobo = SimpleRobo.read(c, dimensions);
            savedData.robos.put(simpleRobo.id, simpleRobo);
        });
        return savedData;
    }

    public Map<UUID, SimpleRobo> getRobos() {
        return robos;
    }
}
