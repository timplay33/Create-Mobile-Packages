package de.theidler.create_mobile_packages.items.robo_bee;

import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RoboBeeItem extends Item {
    public RoboBeeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (itemStack.getItem() instanceof RoboBeeItem) {
            pLevel.addFreshEntity(new RoboBeeEntity(pLevel, ItemStack.EMPTY, pPlayer.blockPosition()));
            itemStack.setCount(itemStack.getCount() - 1);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
