package de.theidler.create_mobile_packages.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock;
import de.theidler.create_mobile_packages.items.bee_portal.BeePortalItem;
import net.minecraft.world.item.Item;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;


public class CMPBlocks {
    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final BlockEntry<BeePortBlock> BEE_PORT = CreateMobilePackages.REGISTRATE.block("bee_port", BeePortBlock::new)
            .initialProperties(SharedProperties::wooden)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(displaySource(CMPDisplaySources.BEE_COUNT))
            .transform(displaySource(CMPDisplaySources.BEE_ETA))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<BeePortalBlock> BEE_PORTAL = CreateMobilePackages.REGISTRATE.block("bee_portal", BeePortalBlock::new)
            .initialProperties(SharedProperties::wooden)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(BeePortalItem::new)
            .transform(customItemModel())
            .register();

    public static void register() {
    }
}
