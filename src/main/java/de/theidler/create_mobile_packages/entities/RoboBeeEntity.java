package de.theidler.create_mobile_packages.entities;

import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity.sendPackageToPlayer;
import static de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity.setOpen;

public class RoboBeeEntity extends Mob {

    private UUID targetPlayerUUID;
    private Vec3 targetVelocity = Vec3.ZERO;
    private Vec3 origin;
    private Vec3 targetOrigin;
    private ItemStack itemStack;

    private enum DroneState {STARTING, MOVING_TO_PLAYER, WAITING, RETURNING, LANDING}

    private DroneState state = DroneState.STARTING;
    private int waitTicks = 10;
    private boolean isBeenDeliverd = false;
    private final DronePortBlockEntity dpbe;

    public RoboBeeEntity(EntityType<? extends Mob> type, Level level, DronePortBlockEntity dpbe) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setNoAi(true);
        this.setPersistenceRequired();
        origin = this.position();
        targetOrigin = origin.add(0,1,0);
        this.dpbe = dpbe;
    }

    public static RoboBeeEntity createEmpty(EntityType<? extends Mob> type, Level level) {
        return new RoboBeeEntity(type, level, null);
    }

    public void setTargetPlayerUUID(UUID uuid) {
        this.targetPlayerUUID = uuid;
    }

    public Player getTargetPlayer() {
        if (this.targetPlayerUUID == null) return null;
        return this.level().getPlayerByUUID(this.targetPlayerUUID);
    }

    public void setOrigin(Vec3 origin) {
        this.origin = origin;
        this.targetOrigin = origin.add(0,1,0);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    private int ticksInBlock = 0;
    private int ticksOnStart = 0;
    @Override
    public void tick() {
        super.tick();

        Player target = getTargetPlayer();
        Vec3 currentPos = this.position();

        switch (state) {
            case STARTING:
                startingState(target);
                break;
            case MOVING_TO_PLAYER:
                movingToPlayerState(target, currentPos);
                break;
            case WAITING:
                waitingState(target);
                break;
            case RETURNING:
                returningState(currentPos);
                break;
            case LANDING:
                landingState();
                break;
        }

        this.setDeltaMovement(targetVelocity);
        this.move(MoverType.SELF, targetVelocity);
    }

    private void updateDisplay(Player player) {
        player.displayClientMessage(Component.literal("Package will arrive in " + (calcETA(player)) + "s"), true);
    }

    private int calcETA(Player player) {
        double distance = player.position().distanceTo(this.position());
        return (int) (distance / CMPConfigs.server().droneSpeed.get()) + 1;
    }

    // No AI goals; movement is entirely controlled via tick().
    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    private void lookAt(BlockPos pos) {
        Vec3 lookAt = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        Vec3 direction = lookAt.subtract(this.position()).normalize();
        this.setYRot((float) Math.toDegrees(Math.atan2(direction.z, direction.x))-90);
        this.setXRot((float) Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))));
    }

    @Override
    public void checkDespawn() {
    }

    // States
    private void startingState(Player target) {
        if (target != null) lookAt(target.blockPosition());
        Vec3 direction = targetOrigin.subtract(this.position()).normalize();
        double speed = 1 / 20.0;
        targetVelocity = new Vec3(0, direction.scale(speed).y, 0);
        ticksOnStart++;
        if (ticksOnStart >= 20) {
            state = DroneState.MOVING_TO_PLAYER;
            setOpen(dpbe, false);
        }
    }

    private void movingToPlayerState(Player target, Vec3 currentPos) {
        if (target != null && target.isAlive()) {
            lookAt(target.blockPosition());
            updateDisplay(target);
            Vec3 desiredTarget = target.position().add(0,2,0);
            if (currentPos.distanceTo(desiredTarget) <= 1.5) {
                state = DroneState.WAITING;
                targetVelocity = Vec3.ZERO;
            } else {
                Vec3 direction = desiredTarget.subtract(currentPos).normalize();
                double speed = CMPConfigs.server().droneSpeed.get() / 20.0;
                targetVelocity = direction.scale(speed);
            }
        } else {
            targetVelocity = Vec3.ZERO;
        }
    }
    private void waitingState(Player target) {
        lookAt(BlockPos.containing(targetOrigin));
        targetVelocity = Vec3.ZERO;
        waitTicks--;
        if (waitTicks <= 0) {
            state = DroneState.RETURNING;
        }
        if (!isBeenDeliverd) {
            sendPackageToPlayer(target, itemStack, this);
            isBeenDeliverd = true;
        }
    }
    private void returningState(Vec3 currentPos) {
        if (targetOrigin == null) {
            targetVelocity = Vec3.ZERO;
            return;
        }
        lookAt(BlockPos.containing(targetOrigin));
        if (currentPos.distanceTo(targetOrigin) <= 0.2) {
            targetVelocity = Vec3.ZERO;
            this.setPos(targetOrigin);
            setOpen(dpbe, true);
            state = DroneState.LANDING;
        } else {
            Vec3 direction = targetOrigin.subtract(currentPos).normalize();
            double speed = CMPConfigs.server().droneSpeed.get() / 20.0;
            targetVelocity = direction.scale(speed);
        }
    }
    private void landingState() {
        Vec3 direction = origin.subtract(this.position()).normalize();
        targetVelocity = direction.scale(1.0 / 20.0);
        ticksInBlock++;
        if (ticksInBlock >= 40) { // 2 Sekunden (40 Ticks)
            setOpen(dpbe, false);
            this.discard();
        }
    }


}
