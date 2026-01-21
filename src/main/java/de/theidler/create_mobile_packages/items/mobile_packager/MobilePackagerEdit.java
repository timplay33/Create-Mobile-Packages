package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class MobilePackagerEdit {
    public ItemStackHandler contents = new ItemStackHandler(9);
    public String address = "";

    public void loadFromStack(ItemStack originalPackage) {
        if (PackageItem.isPackage(originalPackage)) {
            ItemStackHandler packageContents = PackageItem.getContents(originalPackage);
            // Kopiere den Inhalt in den bestehenden handler statt die Referenz zu ersetzen
            for (int i = 0; i < Math.min(contents.getSlots(), packageContents.getSlots()); i++) {
                contents.setStackInSlot(i, packageContents.getStackInSlot(i).copy());
            }
            address = PackageItem.getAddress(originalPackage);
        }
    }

    public ItemStack writeToStack() {
        ItemStack packageItem = PackageItem.containing(contents);
        PackageItem.addAddress(packageItem, address);
        return packageItem;
    }
}
