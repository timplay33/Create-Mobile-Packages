package de.theidler.create_mobile_packages;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SimpleRobo {

    public UUID id;
    public Level level;
    public Vec3 position;
    public boolean invalid;

    public SimpleRobo(UUID id, Level level, Vec3 position) {
        this.id = id;
        this.level = level;
        this.position = position;
    }

    public SimpleRobo(Level level, Vec3 position) {
        this(UUID.randomUUID(), level, position);
    }

    //region Getters/Setters
    public Level getLevel() {
        return level;
    }
    public void setLevel(Level level) {
        this.level = level;
    }
    public Vec3 getPosition() {
        return position;
    }
    public void setPosition(Vec3 position) {
        this.position = position;
    }
    //endregion

    public static SimpleRobo read(CompoundTag tag, DimensionPalette dimensions) {
        UUID id = tag.getUUID("Id");
        Level level = CreateMobilePackages.ROBO_MANAGER.getLevel();

        double x = tag.getDouble("PosX");
        double y = tag.getDouble("PosY");
        double z = tag.getDouble("PosZ");
        Vec3 position = new Vec3(x, y, z);

        return new SimpleRobo(id, level, position);
    }

    public void tick() {
        CreateMobilePackages.ROBO_MANAGER.markDirty();
        CreateMobilePackages.LOGGER.info("SimpleRobo ticked: {}", this.id);
    }

    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Id", id);

        if (position != null) {
            nbt.putDouble("PosX", position.x);
            nbt.putDouble("PosY", position.y);
            nbt.putDouble("PosZ", position.z);
        }

        return nbt;
    }

    public void remove() {
        this.invalid = true;
        CreateMobilePackages.LOGGER.info("SimpleRobo marked for Removal: {}", this.id);
        CreateMobilePackages.ROBO_MANAGER.markDirty();
    }
}
