package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RoboBeeEntity extends RoboEntity {

    public RoboBeeEntity(EntityType<? extends Mob> type, Level level, ItemStack itemStack, Location targetLocation, BlockPos spawnPos) {
        super(type, level, itemStack, targetLocation, spawnPos);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    public RoboBeeEntity(Level level, ItemStack itemStack, Location targetLocation, BlockPos spawnPos) {
        this(CMPEntities.ROBO_BEE_ENTITY.get(), level, itemStack, targetLocation, spawnPos);
    }

    public static RoboBeeEntity createEmpty(EntityType<? extends Mob> type, Level level) {
        return new RoboBeeEntity(type, level, ItemStack.EMPTY, null, new BlockPos(0, 0, 0));
    }

    // No AI goals; movement is entirely controlled via tick().
    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean canCollideWith(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void push(@NotNull Entity entity) {
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void checkDespawn() {
    }

}
