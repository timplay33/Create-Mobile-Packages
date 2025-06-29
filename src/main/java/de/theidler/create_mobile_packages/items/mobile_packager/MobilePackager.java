package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class MobilePackager extends Item {

    public MobilePackager(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            if (pPlayer.isShiftKeyDown()) {
                pPlayer.openMenu(new SimpleMenuProvider((id, inv, player) -> new MobilePackagerMenu(id, inv, this), stack.getDisplayName()));
            } else {
                pPlayer.openMenu(new SimpleMenuProvider((id, inv, player) -> new MobilePackagerEditMenu(id, inv, new MobilePackagerEdit(), PackageItem.containing(List.of())), stack.getDisplayName()));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
