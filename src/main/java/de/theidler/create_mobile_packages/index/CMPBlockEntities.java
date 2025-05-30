package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;

public class CMPBlockEntities {
    public static final BlockEntityEntry<BeePortBlockEntity> BEE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_port", BeePortBlockEntity::new)
            .validBlocks(CMPBlocks.BEE_PORT)
            .register();

    public static final BlockEntityEntry<BeePortalBlockEntity> BEE_PORTAL = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_portal", BeePortalBlockEntity::new)
            .validBlocks(CMPBlocks.BEE_PORTAL)
            .register();


    public static void register() {
    }
}
