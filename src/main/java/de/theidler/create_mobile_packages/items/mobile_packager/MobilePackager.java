package de.theidler.create_mobile_packages.items.mobile_packager;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MobilePackager extends Item {

    public MobilePackager(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            // Open the GUI for the Mobile Packager
            pPlayer.openMenu(new SimpleMenuProvider((id, inv, player) -> new MobilePackagerMenu(id, inv, this), stack.getDisplayName()));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
