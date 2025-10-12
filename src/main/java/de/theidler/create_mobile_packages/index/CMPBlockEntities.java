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

    public static void register() {
    }
}
