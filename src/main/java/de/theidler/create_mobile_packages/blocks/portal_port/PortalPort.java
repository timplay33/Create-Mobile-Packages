package de.theidler.create_mobile_packages.blocks.portal_port;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortalPort extends Block implements IBE<PortalPortBlockEntity>, IWrenchable {
    public static final BooleanProperty IS_OPEN_TEXTURE = BooleanProperty.create("open");
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public PortalPort(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(IS_OPEN_TEXTURE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_OPEN_TEXTURE, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(IS_OPEN_TEXTURE, true)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Class<PortalPortBlockEntity> getBlockEntityClass() {
        return PortalPortBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PortalPortBlockEntity> getBlockEntityType() {
        return CMPBlockEntities.PORTAL_PORT.get();
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }
}
