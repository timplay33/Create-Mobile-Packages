package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class MobilePackagerMenu extends MenuBase<MobilePackager> {
    public MobilePackagerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, extraData);
    }

    public MobilePackagerMenu(int id, Inventory inv, MobilePackager contentHolder) {
        super(CMPMenuTypes.MOBILE_PACKAGER_MENU.get(), id, inv, contentHolder);
    }

    @Override
    public MobilePackager createOnClient(FriendlyByteBuf extraData) {
        return null;
    }

    @Override
    public void initAndReadInventory(MobilePackager contentHolder) {

    }

    @Override
    public void addSlots() {

    }

    @Override
    public void saveData(MobilePackager contentHolder) {

    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }
}
