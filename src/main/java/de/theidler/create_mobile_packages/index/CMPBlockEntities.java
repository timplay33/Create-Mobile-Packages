package de.theidler.create_mobile_packages.index;

import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CMPBlockEntities {

    public static final BlockEntityEntry<BeePortBlockEntity> BEE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_port", BeePortBlockEntity::new)
            .validBlocks(CMPBlocks.BEE_PORT)
            .register();

    public static final BlockEntityEntry<DronePortBlockEntity> DRONE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("drone_port", DronePortBlockEntity::new)
            .validBlocks(CMPBlocks.DRONE_PORT)
            .register();


    public static void register() {
    }

    @Deprecated
    public static class DronePortBlockEntity extends PackagePortBlockEntity {

        public DronePortBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
            super(pType, pPos, pBlockState);
            itemHandler = new ItemHandlerWrapper(inventory);
        }

        @Override
        public void tick() {
            if (!level.isClientSide) {
                tryConvert(level, worldPosition);
            }
        }

        public static void tryConvert(Level level, BlockPos pos) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof DronePortBlockEntity dummy)) return;

            // Save old data
            CompoundTag data = new CompoundTag();
            dummy.saveAdditional(data, level.registryAccess());

            // Replace block
            level.removeBlockEntity(pos);
            level.setBlock(pos, CMPBlocks.BEE_PORT.get().defaultBlockState(), 3);

            // Restore data to new block entity
            BlockEntity newBe = level.getBlockEntity(pos);
            if (newBe instanceof BeePortBlockEntity beePort) {
                beePort.loadWithComponents(data, level.registryAccess());
            }
        }

        @Override
        protected void onOpenChange(boolean open) {

        }
    }
}
