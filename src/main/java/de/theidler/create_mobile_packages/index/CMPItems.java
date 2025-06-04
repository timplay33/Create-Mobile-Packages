package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlock;
import de.theidler.create_mobile_packages.items.bee_portal.BeePortalItem;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


public class CMPItems {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final ItemEntry<PortableStockTicker> PORTABLE_STOCK_TICKER =
            CreateMobilePackages.REGISTRATE.item("portable_stock_ticker", PortableStockTicker::new)
                    .register();

    public static final ItemEntry<RoboBeeItem> ROBO_BEE =
            CreateMobilePackages.REGISTRATE.item("robo_bee", RoboBeeItem::new)
                    .register();

    public static void register() {
    }
}
