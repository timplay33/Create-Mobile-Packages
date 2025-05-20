package de.theidler.create_mobile_packages;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class SimpleRobo {

    public UUID id;
    public boolean invalid;

    public SimpleRobo(UUID id){
        this.id = id;
    }

    public static SimpleRobo read(CompoundTag tag, DimensionPalette dimensions) {
        UUID id = tag.getUUID("Id");

        SimpleRobo simpleRobo = new SimpleRobo(id);
        return simpleRobo;
    }

    public void tick() {
        CreateMobilePackages.ROBO_MANAGER.markDirty();

        CreateMobilePackages.LOGGER.info("{} SimpleRobo ticked", this);
    }

    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Id", id);
        return nbt;
    }
}
