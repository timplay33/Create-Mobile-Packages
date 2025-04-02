package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.isTuned;

public class DroneController extends StockCheckingItem {

    protected String previouslyUsedAddress;

    public DroneController(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, PackageOrder order, IdentifiedInventory ignoredHandler,
                                           String address) {
        boolean result = super.broadcastPackageRequest(type, order, ignoredHandler, address);
        previouslyUsedAddress = address;
        return result;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            InventorySummary summary = getAccurateSummary(stack);

            List<BigItemStack> bigItemStacks = summary.getStacks();

            if(!isTuned(stack)) {
                pPlayer.displayClientMessage(Component.literal("Not linked to Network"), true);
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new DroneControllerMenu(id, inv, bigItemStacks, this),
                    Component.translatable("screen.create_mobile_packages.drone_controller")
            );
            NetworkHooks.openScreen((ServerPlayer) pPlayer, provider);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (id, inv, ply) -> new DroneControllerMenu(id, inv, bigItemStacks, this),
                        Component.literal("Drone Controller")
                ), buf -> {});
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
