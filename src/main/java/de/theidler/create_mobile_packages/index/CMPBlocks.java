package de.theidler.create_mobile_packages.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;


public class CMPBlocks {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final BlockEntry<BeePortBlock> DRONE_PORT = CreateMobilePackages.REGISTRATE.block("drone_port", BeePortBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static void register() {
    }
}
