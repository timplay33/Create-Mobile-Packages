package de.theidler.create_mobile_packages;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RoboManagerSavedData extends SavedData {

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        RoboManager roboManager = CreateMobilePackages.ROBO_MANAGER;
        ListTag listTag = new ListTag();
        for (SimpleRobo simpleRobo : roboManager.getRoboStore()) {
            CompoundTag roboTag = new CompoundTag();
            simpleRobo.save(roboTag);
            listTag.add(roboTag);
        }
        pCompoundTag.put("roboStore", listTag);
        return pCompoundTag;
    }

    public static void load(ServerLevel level) {
        level.getDataStorage().computeIfAbsent(RoboManagerSavedData::load, RoboManagerSavedData::new, "robo_manager");
    }

    public static RoboManagerSavedData load(CompoundTag pCompoundTag) {
        RoboManager roboManager = CreateMobilePackages.ROBO_MANAGER;
        ListTag listTag = pCompoundTag.getList("roboStore", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag roboTag = listTag.getCompound(i);
            SimpleRobo simpleRobo = SimpleRobo.createFromTag(roboTag);
            roboManager.add(simpleRobo);
        }
        return new RoboManagerSavedData();
    }
}
