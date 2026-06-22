package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.blocks.portal_port.PortalPortBlockEntity;

public class CMPBlockEntities {

    public static final BlockEntityEntry<BeePortBlockEntity> BEE_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("bee_port", BeePortBlockEntity::new)
            .validBlocks(CMPBlocks.BEE_PORT)
            .register();

    public static final BlockEntityEntry<PortalPortBlockEntity> PORTAL_PORT = CreateMobilePackages.REGISTRATE
            .blockEntity("portal_port", PortalPortBlockEntity::new)
            .validBlocks(CMPBlocks.PORTAL_PORT)
            .register();

    public static void register() {
    }
}
