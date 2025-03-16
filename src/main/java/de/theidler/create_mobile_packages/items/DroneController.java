package de.theidler.create_mobile_packages.items;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class DroneController extends Item {
    BlockPos stockTickerBlockEntityPos;
    Level stockTickerBlockEntityLevel;

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
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) == null) return super.useOn(pContext);

        stockTickerBlockEntityPos = pContext.getClickedPos();
        stockTickerBlockEntityLevel = pContext.getLevel();
/*
        if (stockTickerBlockEntityLevel.getBlockEntity(stockTickerBlockEntityPos) != null){
            if (stockTickerBlockEntityLevel.getBlockEntity(stockTickerBlockEntityPos).getClass() == StockTickerBlockEntity.class) {
                stockTickerBlockEntity = (StockTickerBlockEntity) level.getBlockEntity(pos);
            }
        }*/
        return super.useOn(pContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new DroneControllerMenu(id, inv),
                    Component.translatable("screen.create_mobile_packages.drone_controller")
            );
            NetworkHooks.openScreen((ServerPlayer) pPlayer, provider);
        }
    /*    if (stockTickerBlockEntityPos == null || stockTickerBlockEntityLevel == null) return super.use(pLevel, pPlayer, pUsedHand);

        StockTickerBlockEntity stockTickerBlockEntity = (StockTickerBlockEntity) stockTickerBlockEntityLevel.getBlockEntity(stockTickerBlockEntityPos);

        if (stockTickerBlockEntity == null) return super.use(pLevel, pPlayer, pUsedHand);*/
/*

        // from StockTickerInteractionHandler.interactWithLogisticsManagerAt
        if (pPlayer instanceof ServerPlayer sp) {
            boolean showLockOption =
                    stockTickerBlockEntity.behaviour.mayAdministrate(pPlayer) && Create.LOGISTICS.isLockable(stockTickerBlockEntity.behaviour.freqId);
            boolean isCurrentlyLocked = Create.LOGISTICS.isLocked(stockTickerBlockEntity.behaviour.freqId);

            NetworkHooks.openScreen(sp, stockTickerBlockEntity.new RequestMenuProvider(), buf -> {
                buf.writeBoolean(showLockOption);
                buf.writeBoolean(isCurrentlyLocked);
                buf.writeBlockPos(stockTickerBlockEntity.getBlockPos());
            });
            stockTickerBlockEntity.getRecentSummary()
                    .divideAndSendTo(sp, stockTickerBlockEntity.getBlockPos());
        }

        return super.use(pLevel, pPlayer, pUsedHand);*/
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }
}
