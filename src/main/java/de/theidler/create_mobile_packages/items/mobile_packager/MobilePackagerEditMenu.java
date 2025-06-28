package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class MobilePackagerEditMenu extends MenuBase<MobilePackagerEdit> {

    public ItemStack originalPackage;

    public MobilePackagerEditMenu(MenuType<MobilePackagerEditMenu> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, new MobilePackagerEdit(), extraData.readItem());
    }

    public MobilePackagerEditMenu(int id, Inventory inv, MobilePackagerEdit contentHolder, ItemStack originalPackage) {
        super(CMPMenuTypes.MOBILE_PACKAGER_EDIT_MENU.get(), id, inv, contentHolder);
        this.originalPackage = originalPackage;
        contentHolder.loadFromStack(originalPackage);
        CreateMobilePackages.LOGGER.info("Creating MobilePackagerEditMenu with original package: {}", originalPackage);
    }

    @Override
    protected MobilePackagerEdit createOnClient(FriendlyByteBuf extraData) {
        return null;
    }

    @Override
    protected void initAndReadInventory(MobilePackagerEdit contentHolder) {
    }

    @Override
    protected void addSlots() {
        int slotX = 27;
        int slotY = 28;
        for (int i = 0; i < 9; i++)
            addSlot(new SlotItemHandler(contentHolder.contents, i, slotX + 20 * i, slotY));
        addPlayerSlots(33, 142);
    }

    @Override
    protected void saveData(MobilePackagerEdit contentHolder) {
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = clickedSlot.getItem();

        boolean success = !moveItemStackTo(stack, 0, slots.size(), false);

        return success ? ItemStack.EMPTY : stack;

    }

    public void confirm() {
        ItemStack box = contentHolder.writeToStack();
        player.getInventory().placeItemBackInInventory(box);
        player.closeContainer();
    }
}
