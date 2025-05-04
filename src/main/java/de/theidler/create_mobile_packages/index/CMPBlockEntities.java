package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;

public class CMPBlockEntities {

    public static final BlockEntityEntry<BeePortBlockEntity> DRONE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("drone_port", BeePortBlockEntity::new)
            .validBlocks(CMPBlocks.DRONE_PORT)
            .register();

    public static void register() {
    }
}
