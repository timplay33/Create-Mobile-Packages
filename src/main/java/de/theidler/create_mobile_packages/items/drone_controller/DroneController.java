package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem.isTuned;

public class DroneController extends StockCheckingItem implements MenuProvider{

    protected String previouslyUsedAddress;

    public DroneController(Properties pProperties) {
        super(pProperties.stacksTo(1));
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
                pPlayer.displayClientMessage(Component.translatable("item.create_mobile_packages.drone_controller.not_linked"), true);
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
            /*
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new DroneControllerMenu(id, inv, this),
                    Component.translatable("item.create_mobile_packages.drone_controller")
            );
            NetworkHooks.openScreen((ServerPlayer) pPlayer, provider);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (id, inv, ply) -> new DroneControllerMenu(id, inv, this),
                        Component.translatable("item.create_mobile_packages.drone_controller")
                ), buf -> {});
            }*/
            pPlayer.openMenu(this, pPlayer.blockPosition());
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.summary").withStyle(ChatFormatting.YELLOW));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.condition1").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.behaviour1").withStyle(ChatFormatting.YELLOW));
            tooltipComponents.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.condition2").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.behaviour2").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipComponents.add(Component.translatable("create.tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DroneControllerMenu(i, inventory, this);
    }
}
