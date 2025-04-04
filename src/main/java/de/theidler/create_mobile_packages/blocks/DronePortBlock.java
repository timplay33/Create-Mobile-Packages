package de.theidler.create_mobile_packages.blocks;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.theidler.create_mobile_packages.index.CMPBlockEntities;
import de.theidler.create_mobile_packages.index.CMPBlocks;
import de.theidler.create_mobile_packages.index.CMPShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class DronePortBlock extends Block implements IBE<DronePortBlockEntity>, IWrenchable {
    public static final VoxelShape DRONE_PORT_SHAPE = CMPShapes.shape(0, 0, 0, 16, 2, 16).add(7, 2, 7,9, 4, 9).build();

    public DronePortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return DRONE_PORT_SHAPE;
    }

    @Override
    public Class<DronePortBlockEntity> getBlockEntityClass() {
        return DronePortBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DronePortBlockEntity> getBlockEntityType() {
        return CMPBlockEntities.DRONE_PORT.get();
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            withBlockEntityDo(pLevel, pPos, blockEntity -> {
                SimpleContainer container = new SimpleContainer(blockEntity.getInventory().getSlots());

                for (int i = 0; i < blockEntity.getInventory().getSlots(); i++) {
                    container.setItem(i, blockEntity.getInventory().getStackInSlot(i));
                }

                Containers.dropContents(pLevel, pPos, container);
            });
            super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = super.getDrops(pState, pParams);
        if (drops.isEmpty()) {
            drops.add(new ItemStack(CMPBlocks.DRONE_PORT.asItem(), 1));
        }
        return drops;
    }
}