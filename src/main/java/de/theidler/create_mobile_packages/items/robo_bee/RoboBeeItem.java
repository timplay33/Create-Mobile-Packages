package de.theidler.create_mobile_packages.items.robo_bee;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RoboBeeItem extends Item {

    public RoboBeeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack offhandItem = player.getOffhandItem();

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        ItemStack packageItem = ItemStack.EMPTY;
        if (PackageItem.isPackage(offhandItem)){
            packageItem = offhandItem.copy();
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
        RoboBeeEntity roboBee = new RoboBeeEntity(
                level,
                packageItem,
                null,
                pos
        );

        if (!roboBee.getItemStack().isEmpty()) {
            roboBee.setPackageHeightScale(1.0F);
        }

        level.addFreshEntity(roboBee);
        context.getItemInHand().shrink(1);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }
}
