package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeePortStorage {
    private final Map<Level, List<BeePortBlockEntity>> BeePorts = new HashMap<>();
    private final Map<Level, List<BeePortalBlockEntity>> BeePortals = new HashMap<>();

    public List<BeePortBlockEntity> getBeePorts(Level level) {
        return BeePorts.get(level);
    }

    public Map<Level, List<BeePortBlockEntity>> getAllBeePorts(Level exclude) {
        return BeePorts.entrySet().stream()
                .filter(entry -> entry != exclude)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Level, List<BeePortBlockEntity>> getAllBeePorts() {
        return BeePorts;
    }

    public Map<Level, List<BeePortalBlockEntity>> getAllBeePortals(Level exclude) {
        return BeePortals.entrySet().stream()
                .filter(entry -> entry != exclude)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Level, List<BeePortalBlockEntity>> getAllBeePortals() {
        return BeePortals;
    }

    public List<BeePortalBlockEntity> getBeePortals(Level level) {
        return BeePortals.get(level);
    }

    public void addBeePort(BeePortBlockEntity BeePort, Level level) {
        if (BeePorts.get(level).stream().noneMatch(BP -> BeePort.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePort.getBlockPos() == BP.getBlockPos()))
            BeePorts.get(level).add(BeePort);
    }

    public void addBeePortal(BeePortalBlockEntity BeePortal, Level level) {
        if (BeePortals.get(level).stream().noneMatch(BP -> BeePortal.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePortal.getBlockPos() == BP.getBlockPos()))
            BeePortals.get(level).add(BeePortal);
    }

    public void removeBeePort(BeePortBlockEntity BeePort, Level level) {
        BeePorts.get(level).removeIf(BP -> BeePort.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePort.getBlockPos() == BP.getBlockPos());
    }

    public void removeBeePortal(BeePortalBlockEntity BeePortal, Level level) {
        BeePortals.get(level).removeIf(BP -> BeePortal.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePortal.getBlockPos() == BP.getBlockPos());
    }

    public void addBeePortalLevel(Level level) {
        if (!BeePorts.containsKey(level))
            BeePorts.put(level, new ArrayList<>());
    }

    public void addBeePortLevel(Level level) {
        if (!BeePortals.containsKey(level))
            BeePortals.put(level, new ArrayList<>());
    }

    public boolean hasLevel(Level level) {
        return BeePorts.containsKey(level);
    }
}
