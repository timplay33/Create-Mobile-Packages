package de.theidler.create_mobile_packages.items;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.slf4j.Logger;

public class DroneController extends Item {
    StockTickerBlockEntity stockTickerBlockEntity;

    private static final Logger LOGGER = LogUtils.getLogger();
    public DroneController(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(!(pEntity instanceof Player player)) return;

        //player.getInventory().offhand.add(CMPBlocks.DRONE_PORT.asStack());
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        LOGGER.info("{} {} {}",level.getBlockEntity(pos), level.getBlockEntity(pos).getClass() == StockTickerBlockEntity.class, !level.isClientSide);

        if (level.getBlockEntity(pos) != null){
            if (level.getBlockEntity(pos).getClass() == StockTickerBlockEntity.class) {
                stockTickerBlockEntity = (StockTickerBlockEntity) level.getBlockEntity(pos);
                if (!level.isClientSide) {
                    // Open the menu
                    player.openMenu(new SimpleMenuProvider(
                            (id, inventory, p) -> new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), id, inventory,stockTickerBlockEntity),
                            Component.literal("item.create_mobile_packages.drone_controller")
                    ));
                }
            }
        }
        return super.useOn(pContext);
    }
/*
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            NetworkHooks.openScreen((ServerPlayer) pPlayer, new SimpleMenuProvider(
                    (id, inventory, p) -> new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), id, inventory,stockTickerBlockEntity),
                    Component.translatable("item.create_mobile_packages.drone_controller")
            ));
        }
        return InteractionResultHolder.success(itemStack);
    }*/
    /*
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (!isTuned(pStack))
            return;

        CompoundTag tag = pStack.getTag()
                .getCompound(BLOCK_ENTITY_TAG);
        if (!tag.hasUUID("Freq"))
            return;

        CreateLang.translate("logistically_linked.tooltip")
                .style(ChatFormatting.GOLD)
                .addTo(pTooltipComponents);

        CreateLang.translate("logistically_linked.tooltip_clear")
                .style(ChatFormatting.GRAY)
                .addTo(pTooltipComponents);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        return useOn;
    }*/

    /*private static final Logger LOGGER = LogUtils.getLogger();
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        LOGGER.info("{} {}", itemStack.getItem(), !pLevel.isClientSide);
        if (!pLevel.isClientSide) {
            ;
            LOGGER.info("{}", StockTickerInteractionHandler.interactWithLogisticsManagerAt(pPlayer, pLevel, pPlayer.getOnPos()));
        }
        return InteractionResultHolder.success(itemStack) ;
    }*/
}
