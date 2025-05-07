package de.theidler.create_mobile_packages.items.robo_bee;

import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RoboBeeItem extends Item {
    public RoboBeeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (itemStack.getItem() instanceof RoboBeeItem) {
            pLevel.addFreshEntity(new RoboBeeEntity(pLevel, ItemStack.EMPTY, null, getTargetPos(pPlayer)));
            itemStack.setCount(itemStack.getCount() - 1);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    private BlockPos getTargetPos(Player pPlayer) {
        double rayTraceDistance = 5.0D;
        Vec3 eyePos = pPlayer.getEyePosition();
        Vec3 lookVec = pPlayer.getViewVector(1.0F);
        BlockHitResult result = pPlayer.level().clip(
                new ClipContext(
                        eyePos,
                        eyePos.add(lookVec.scale(rayTraceDistance)),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        pPlayer
                )
        );

        Vec3 hitPos;
        if (result.getType() == BlockHitResult.Type.MISS) {
            hitPos = eyePos.add(lookVec.scale(1.0));
        } else {
            hitPos = result.getLocation().subtract(lookVec.scale(0.3));
        }
        return BlockPos.containing(hitPos);
    }
}
