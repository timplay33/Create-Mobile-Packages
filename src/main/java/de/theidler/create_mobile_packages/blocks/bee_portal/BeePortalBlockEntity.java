package de.theidler.create_mobile_packages.blocks.bee_portal;

import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.Location;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.RequestDimensionTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

import static de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock.IS_OPEN_TEXTURE;

/**
 * Represents a Drone Portal block entity that handles the teleportation of Robo Bees
 * to other dimensions.
 */
public class BeePortalBlockEntity extends BlockEntity {
    private final Queue<RoboEntity> entityLandingQueue = new ArrayDeque<>();
    private final Queue<RoboEntity> entityLaunchingQueue = new ArrayDeque<>();

    /**
     * Constructor for the BeePortalBlockEntity.
     *
     * @param pType       The type of the block entity.
     * @param pPos        The position of the block in the world.
     * @param pBlockState The state of the block.
     */
    public BeePortalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    /**
     * Sets the open state of the drone portal and updates the block state and sound.
     *
     * @param entity The drone portal entity.
     * @param open   Whether the portal is open.
     */
    public static void setOpen(BeePortalBlockEntity entity, boolean open) {
        if (entity == null || entity.level == null) return;
        entity.level.setBlockAndUpdate(entity.getBlockPos(), entity.getBlockState().setValue(IS_OPEN_TEXTURE, open));
        entity.level.playSound(null, entity.getBlockPos(), open ? SoundEvents.PORTAL_TRIGGER : SoundEvents.PORTAL_AMBIENT,
                SoundSource.BLOCKS);
    }

    public boolean sendDrone(@NotNull RoboEntity re) {
        if (!entityLandingQueue.isEmpty() && !entityLandingQueue.peek().equals(re)
                || !entityLaunchingQueue.isEmpty() && !entityLaunchingQueue.peek().equals(re))
            return false;

        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Player targetPlayer = re.getTargetPlayer();
        Level targetLevel = targetBlock == null ? targetPlayer.level() : targetBlock.getLevel();
        if (targetLevel == null) return false;
        Vec3 spawnPos = getBlockPos().getCenter();
        if (targetBlock != null && !targetBlock.canAcceptEntity(re, !re.getItemStack().isEmpty()))
            return false;

        CMPPackets.getChannel()
                .sendToServer(new RequestDimensionTeleport(targetLevel.dimension().location(), spawnPos, targetBlock == null ? targetPlayer.blockPosition() : targetBlock.getBlockPos(), re.getItemStack()));
        return true;
    }

    /**
     * Called when the block entity is loaded. Registers the entity with the tracker.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            storage.add(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            storage.remove(this);
            if (!entityLandingQueue.isEmpty() || !entityLaunchingQueue.isEmpty())
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), CMPItems.ROBO_BEE.asStack(entityLandingQueue.size() + entityLaunchingQueue.size())));
        }

        super.setRemoved();
    }

    public synchronized void tryAddToLandingQueue(RoboEntity entity) {
        if (entity != null && entityLandingQueue.stream().noneMatch(e -> e == entity)) {
            BeePortalBlockEntity.setOpen(this, true);
            entityLandingQueue.add(entity);
        }
    }

    public synchronized void tryRemoveFromLandingQueue(RoboEntity entity) {
        if (!entityLandingQueue.isEmpty() && entityLandingQueue.peek() == entity)
            entityLandingQueue.remove(entity);

        if (entityLandingQueue.isEmpty() && entityLaunchingQueue.isEmpty())
            BeePortalBlockEntity.setOpen(this, false);
    }

    public synchronized void tryAddToLaunchingQueue(RoboEntity entity) {
        if (entity != null && entityLaunchingQueue.stream().noneMatch(e -> e == entity)) {
            BeePortalBlockEntity.setOpen(this, true);
            entityLaunchingQueue.add(entity);
        }
    }

    public synchronized void tryRemoveFromLaunchingQueue(RoboEntity entity) {
        if (!entityLaunchingQueue.isEmpty() && entityLaunchingQueue.peek() == entity)
            entityLaunchingQueue.remove(entity);

        if (entityLandingQueue.isEmpty() && entityLaunchingQueue.isEmpty())
            BeePortalBlockEntity.setOpen(this, false);
    }

    public boolean isLandingPeek(RoboEntity entity) {
        return entityLandingQueue.peek() == entity;
    }

    public boolean isLaunchingPeek(RoboEntity entity) {
        return entityLaunchingQueue.peek() == entity;
    }

    public Location location() {
        Level level = getLevel();
        return level == null ? new Location(getBlockPos(), null) : new Location(getBlockPos(), level.dimensionType());
    }
}
