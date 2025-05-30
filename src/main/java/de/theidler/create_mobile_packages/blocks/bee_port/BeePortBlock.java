package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.CMPShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeePortBlock extends Block implements IBE<BeePortBlockEntity>, IWrenchable {
    public static final BooleanProperty IS_OPEN_TEXTURE = BooleanProperty.create("open");
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public BeePortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(IS_OPEN_TEXTURE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(IS_OPEN_TEXTURE, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(IS_OPEN_TEXTURE, false).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return CMPShapes.DRONE_PORT_SHAPE.get(facing);
    }

    @Override
    public Class<BeePortBlockEntity> getBlockEntityClass() {
        return BeePortBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BeePortBlockEntity> getBlockEntityType() {
        return CMPBlockEntities.BEE_PORT.get();
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        IBE.onRemove(pState, pLevel, pPos, pNewState);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, @NotNull LootParams.Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);
        if (drops.isEmpty())
            drops.add(new ItemStack(CMPBlocks.BEE_PORT.asItem(), 1));
        return drops;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand handIn,
                                          @NotNull BlockHitResult hit) {
        return onBlockEntityUse(worldIn, pos, be -> be.use(player));
    }
}