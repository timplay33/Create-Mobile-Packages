package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker;
import de.theidler.create_mobile_packages.items.robo_bee.RoboBeeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CMPItems {

    static {
        CreateMobilePackages.REGISTRATE.setCreativeTab(CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB);
    }

    public static final ItemEntry<PortableStockTicker> PORTABLE_STOCK_TICKER =
            CreateMobilePackages.REGISTRATE.item("portable_stock_ticker", PortableStockTicker::new)
                    .register();

    public static final ItemEntry<DroneController> DRONE_CONTROLLER =
            CreateMobilePackages.REGISTRATE.item("drone_controller", DroneController::new)
                    .register();

    public static final ItemEntry<RoboBeeItem> ROBO_BEE =
            CreateMobilePackages.REGISTRATE.item("robo_bee",RoboBeeItem::new)
                    .register();

    public static void register() {
    }

    @Deprecated
    public static class DroneController extends Item {
        public DroneController(Properties properties) {
            super(properties);
        }

        @Override
        public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
            if (!pLevel.isClientSide && pEntity instanceof Player player) {
                ItemStack replacement = new ItemStack(CMPItems.PORTABLE_STOCK_TICKER.get());
                replacement.setTag(pStack.getTag()); // preserve NBT
                player.getInventory().setItem(pSlotId, replacement);
            }
        }
    }
}
