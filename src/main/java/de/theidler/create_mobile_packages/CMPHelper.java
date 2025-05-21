package de.theidler.create_mobile_packages;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

    public static Vec3 readVec3FromTag(CompoundTag tag, String key) {
        double x = tag.getDouble(key + "X");
        double y = tag.getDouble(key + "Y");
        double z = tag.getDouble(key + "Z");
        return new Vec3(x, y, z);
    }

    public static CompoundTag writeVec3ToTag(CompoundTag tag, String key, Vec3 vec) {
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
        return tag;
    }
}
