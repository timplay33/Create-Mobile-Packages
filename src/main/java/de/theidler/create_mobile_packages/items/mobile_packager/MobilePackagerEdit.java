package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class MobilePackagerEdit {
    public ItemStackHandler contents = new ItemStackHandler(9); // or however many items
    public String address = "";

    public void loadFromStack(ItemStack originalPackage) {
        if (PackageItem.isPackage(originalPackage)) {
            contents = PackageItem.getContents(originalPackage);
            address = PackageItem.getAddress(originalPackage);
        }
    }

    public ItemStack writeToStack() {
        ItemStack packageItem = PackageItem.containing(contents);
        PackageItem.addAddress(packageItem, address);
        return packageItem;
    }
}
