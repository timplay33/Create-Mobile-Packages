package de.theidler.create_mobile_packages.index;

import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

public class CMPBlockEntities {
    public static final BeePortStorage beePortStorage = new BeePortStorage();
    public static final BlockEntityEntry<BeePortBlockEntity> BEE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_port", BeePortBlockEntity::new)
            .validBlocks(CMPBlocks.BEE_PORT)
            .register();

    public static final BlockEntityEntry<BeePortalBlockEntity> BEE_PORT_PORTAL = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_port_portal", BeePortalBlockEntity::new)
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
            itemHandler = LazyOptional.of(() -> inventory);
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
            dummy.saveAdditional(data);

            // Replace block
            level.removeBlockEntity(pos);
            level.setBlock(pos, CMPBlocks.BEE_PORT.get().defaultBlockState(), 3);

            // Restore data to new block entity
            BlockEntity newBe = level.getBlockEntity(pos);
            if (newBe instanceof BeePortBlockEntity beePort) {
                beePort.load(data);
            }
        }

        @Override
        protected void onOpenChange(boolean open) {

        }
    }
}
