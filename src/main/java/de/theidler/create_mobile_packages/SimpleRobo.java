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
    public Vec3 deltaMovement;
    public boolean invalid;

    public SimpleRobo(UUID id, Level level, Vec3 position, Vec3 deltaMovement) {
        this.id = id;
        this.level = level;
        this.position = position;
        this.deltaMovement = deltaMovement;
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
    public Vec3 getDeltaMovement() {
        return deltaMovement;
    }
    public void setDeltaMovement(Vec3 deltaMovement) {
        this.deltaMovement = deltaMovement;
    }
    //endregion

    public static SimpleRobo read(CompoundTag tag, DimensionPalette dimensions) {
        UUID id = tag.getUUID("Id");
        Level level = CreateMobilePackages.ROBO_MANAGER.getLevel();

        Vec3 position = CMPHelper.readVec3FromTag(tag, "Pos");
        Vec3 deltaMovement = CMPHelper.readVec3FromTag(tag, "Delta");

        return new SimpleRobo(id, level, position, deltaMovement);
    }

    public void tick() {
        if (deltaMovement != null && !deltaMovement.equals(Vec3.ZERO)) {
            position = position.add(deltaMovement);
        }

        CreateMobilePackages.ROBO_MANAGER.markDirty();
        CreateMobilePackages.LOGGER.info("SimpleRobo ticked: {} at {} {} {}", this.id, this.position.x, this.position.y, this.position.z);
    }

    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Id", id);

        nbt = CMPHelper.writeVec3ToTag(nbt, "Pos", position);
        if (deltaMovement != null)
            nbt = CMPHelper.writeVec3ToTag(nbt, "Delta", deltaMovement);

        return nbt;
    }

    public void remove() {
        this.invalid = true;
        CreateMobilePackages.LOGGER.info("SimpleRobo marked for Removal: {}", this.id);
        CreateMobilePackages.ROBO_MANAGER.markDirty();
    }
}
