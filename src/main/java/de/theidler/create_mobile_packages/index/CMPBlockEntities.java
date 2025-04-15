package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.drone_port.DronePortBlockEntity;

public class CMPBlockEntities {

    public static final BlockEntityEntry<DronePortBlockEntity> DRONE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("drone_port", DronePortBlockEntity::new)
            .validBlocks(CMPBlocks.DRONE_PORT)
            .register();

    public static void register() {
    }
}
