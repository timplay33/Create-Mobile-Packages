package de.theidler.create_mobile_packages.blocks.bee_portal;

import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

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

    public boolean sendDrone(@NotNull RoboEntity re) {
        if (!entityLandingQueue.isEmpty() && !entityLandingQueue.peek().equals(re)
                || !entityLaunchingQueue.isEmpty() && !entityLaunchingQueue.peek().equals(re))
            return false;

        BeePortBlockEntity targetBlock = re.getTargetBlockEntity();
        Player targetPlayer = re.getTargetPlayer();
        Level targetLevel = targetBlock == null ? targetPlayer.level() : targetBlock.getLevel();
        if (targetLevel == null) return false;
        if (targetBlock != null && !targetBlock.canAcceptEntity(re, !re.getItemStack().isEmpty()))
            return false;

        CMPPackets.getChannel()
                .sendToServer(new RequestDimensionTeleport(targetLevel.dimension().location(), getBlockPos().getCenter(), targetBlock == null ? targetPlayer.blockPosition() : targetBlock.getBlockPos(), re.getItemStack()));
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
            if (storage.getCurrentPortalToConnect() != null)
                storage.createConnection(this, storage.getCurrentPortalToConnect());

            if (storage.getPortalConnections(this).isEmpty())
                serverLevel.getBlockState(getBlockPos()).setValue(BeePortalBlock.IS_OPEN_TEXTURE, false);
            else
                serverLevel.getBlockState(getBlockPos()).setValue(BeePortalBlock.IS_OPEN_TEXTURE, true);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            storage.remove(this);
            if (!entityLandingQueue.isEmpty() || !entityLaunchingQueue.isEmpty())
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), CMPItems.ROBO_BEE.asStack(entityLandingQueue.size() + entityLaunchingQueue.size())));
        }

        super.setRemoved();
    }

    public synchronized void tryAddToLandingQueue(@NotNull RoboEntity entity) {
        if (entityLandingQueue.stream().noneMatch(e -> e == entity))
            entityLandingQueue.add(entity);
    }

    public synchronized void tryRemoveFromLandingQueue(@NotNull RoboEntity entity) {
        if (!entityLandingQueue.isEmpty())
            entityLandingQueue.remove(entity);
    }

    public synchronized void tryAddToLaunchingQueue(@NotNull RoboEntity entity) {
        if (entityLaunchingQueue.stream().noneMatch(e -> e == entity))
            entityLaunchingQueue.add(entity);
    }

    public synchronized void tryRemoveFromLaunchingQueue(@NotNull RoboEntity entity) {
        if (!entityLaunchingQueue.isEmpty())
            entityLaunchingQueue.remove(entity);
    }

    public boolean isLandingPeek(RoboEntity entity) {
        return entityLandingQueue.peek() == entity;
    }

    public boolean isLaunchingPeek(RoboEntity entity) {
        return entityLaunchingQueue.peek() == entity;
    }
}
