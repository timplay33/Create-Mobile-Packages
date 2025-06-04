package de.theidler.create_mobile_packages.items.bee_portal;

import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BeePortalItem extends BlockItem {
    public BeePortalItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack offhandItem = player.getOffhandItem();
        BlockPos pos = context.getClickedPos();
        if (player.isShiftKeyDown() && offhandItem.getItem() instanceof BeePortalItem) {
            BeePortalBlockEntity beePortal = serverLevel.getBlockEntity(pos) instanceof BeePortalBlockEntity p ? p : null;
            if (beePortal == null) return InteractionResult.PASS;
            BeePortStorage storage = BeePortStorage.get(serverLevel);
            if (!storage.trySetPortalToConnect(beePortal)) {
                player.displayClientMessage(Component.translatable("item.create_mobile_packages.bee_portal.already_connecting"), true);
                return InteractionResult.FAIL;
            }
        } else
            this.place(new BlockPlaceContext(context));

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
    }
}