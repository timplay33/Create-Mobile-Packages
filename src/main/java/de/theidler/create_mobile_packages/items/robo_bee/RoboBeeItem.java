package de.theidler.create_mobile_packages.items.robo_bee;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.StockCheckingItem;
import de.theidler.create_mobile_packages.robo.RoboManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.networkFromStack;

public class RoboBeeItem extends StockCheckingItem {

    public RoboBeeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (super.useOn(context) != InteractionResult.PASS) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack offhandItem = player.getOffhandItem();

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        ItemStack packageItem = ItemStack.EMPTY;
        if (PackageItem.isPackage(offhandItem) && CMPConfigs.server().allowRoboBeeSpawnPackageTransport.get()) {
            packageItem = offhandItem.copy();
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
        if (level instanceof ServerLevel serverLevel) {
            UUID networkId = networkFromStack(context.getItemInHand());
            UUID finalNetworkId = networkId != null ? networkId : UUID.randomUUID();
            RoboManager.get(serverLevel).newRobo(serverLevel, packageItem, pos, finalNetworkId, true);
        }
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.create_mobile_packages.robo_bee.robo_bee").withStyle(ChatFormatting.GRAY));
        if (CMPConfigs.server().allowRoboBeeSpawnPackageTransport.get()) {
            pTooltipComponents.add(Component.translatable("tooltip.create_mobile_packages.robo_bee.package_transport").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}