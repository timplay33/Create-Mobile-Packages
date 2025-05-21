package de.theidler.create_mobile_packages;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CMPHelper {
    public static void sendPlayerChatMessage(Player player, String message) {

    }

    public static Player getPlayerFromPackageItemStack(ItemStack itemStack, Level level) {
        if (!PackageItem.isPackage(itemStack)) return null;
        for (Player player : level.players()) {
            if (doesPlayerNameMatchAddress(player, PackageItem.getAddress(itemStack))) {
                return player;
            }
        }
        return null;
    }

    public static boolean doesPlayerNameMatchAddress(Player player, String address) {
        return player.getName().getString().equals(address);
    }
}
