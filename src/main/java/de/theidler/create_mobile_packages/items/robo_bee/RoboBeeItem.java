package de.theidler.create_mobile_packages.items.robo_bee;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        if (PackageItem.isPackage(offhandItem) && CMPConfigs.server().allowRoboBeeSpawnPackageTransport.get()){
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
        roboBee.setRequest(false);
        context.getItemInHand().shrink(1);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.create_mobile_packages.robo_bee.robo_bee").withStyle(ChatFormatting.GRAY));
        if (CMPConfigs.server().allowRoboBeeSpawnPackageTransport.get()) {
            pTooltipComponents.add(Component.translatable("tooltip.create_mobile_packages.robo_bee.package_transport").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}