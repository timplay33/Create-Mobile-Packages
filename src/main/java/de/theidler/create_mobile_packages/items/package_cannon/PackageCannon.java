package de.theidler.create_mobile_packages.items.package_cannon;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PackageCannon extends Item {

    public PackageCannon(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack item = player.getOffhandItem();
        if (PackageItem.isPackage(item)) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            Vec3 pPos = player.position();
            Vec3 pDir = player.getViewVector(0);
            PackageEntity pEnt = PackageEntity.fromItemStack(level, pPos, item);
            pEnt.addDeltaMovement(pDir.scale(10));
            level.addFreshEntity(pEnt);
        }
        return super.use(level, player, usedHand);
    }
}
