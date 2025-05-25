package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.theidler.create_mobile_packages.index.CMPBlockEntities.beePortStorage;

public class BeePortStorage {
    private final Map<DimensionType, List<BeePortBlockEntity>> beePorts = new HashMap<>();
    private final Map<DimensionType, List<BeePortalBlockEntity>> beePortals = new HashMap<>();

    public List<BeePortBlockEntity> getBeePorts(DimensionType dim) {
        return beePorts.get(dim);
    }

    public Map<DimensionType, List<BeePortBlockEntity>> getAllBeePorts(DimensionType exclude) {
        return beePorts.entrySet().stream()
                .filter(entry -> entry.getKey() != exclude)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<DimensionType, List<BeePortBlockEntity>> getAllBeePorts() {
        return beePorts;
    }

    public Map<DimensionType, List<BeePortalBlockEntity>> getAllBeePortals(DimensionType exclude) {
        return beePortals.entrySet().stream()
                .filter(entry -> entry.getKey() != exclude)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<DimensionType, List<BeePortalBlockEntity>> getAllBeePortals() {
        return beePortals;
    }

    public List<BeePortalBlockEntity> getBeePortals(DimensionType dim) {
        return beePortals.get(dim);
    }

    public void addBeePort(BeePortBlockEntity BeePort, DimensionType dim) {
        if (!beePortStorage.hasLevel(dim)) {
            beePortStorage.addBeePortLevel(dim);
            beePortStorage.addBeePortalLevel(dim);
        }

        if (beePorts.get(dim).stream().noneMatch(BP -> BeePort.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePort.getBlockPos() == BP.getBlockPos()))
            beePorts.get(dim).add(BeePort);
    }

    public void addBeePortal(BeePortalBlockEntity BeePortal, DimensionType dim) {
        if (!beePortStorage.hasLevel(dim)) {
            beePortStorage.addBeePortLevel(dim);
            beePortStorage.addBeePortalLevel(dim);
        }

        if (beePortals.get(dim).stream().noneMatch(BP -> BeePortal.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePortal.getBlockPos() == BP.getBlockPos()))
            beePortals.get(dim).add(BeePortal);
    }

    public void removeBeePort(BeePortBlockEntity BeePort, DimensionType dim) {
        beePorts.get(dim).removeIf(BP -> BeePort.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePort.getBlockPos() == BP.getBlockPos());
    }

    public void removeBeePortal(BeePortalBlockEntity BeePortal, DimensionType dim) {
        beePortals.get(dim).removeIf(BP -> BeePortal.getLevel().dimensionType() == BP.getLevel().dimensionType() && BeePortal.getBlockPos() == BP.getBlockPos());
    }

    public void addBeePortalLevel(DimensionType dim) {
        if (!beePorts.containsKey(dim))
            beePorts.put(dim, new ArrayList<>());
    }

    public void addBeePortLevel(DimensionType dim) {
        if (!beePortals.containsKey(dim))
            beePortals.put(dim, new ArrayList<>());
    }

    public boolean hasLevel(DimensionType dim) {
        return beePorts.containsKey(dim);
    }
}
