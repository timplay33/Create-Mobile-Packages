package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

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
    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler,
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
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (Screen.hasShiftDown()) {
            pTooltip.add(Component.literal(""));
            pTooltip.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.summary").withStyle(ChatFormatting.YELLOW));
            pTooltip.add(Component.literal(""));
            pTooltip.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.condition1").withStyle(ChatFormatting.GRAY));
            pTooltip.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.behaviour1").withStyle(ChatFormatting.YELLOW));
            pTooltip.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.condition2").withStyle(ChatFormatting.GRAY));
            pTooltip.add(Component.translatable("item.create_mobile_packages.drone_controller.tooltip.behaviour2").withStyle(ChatFormatting.YELLOW));
        } else {
            pTooltip.add(Component.translatable("create.tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }


    }
}
