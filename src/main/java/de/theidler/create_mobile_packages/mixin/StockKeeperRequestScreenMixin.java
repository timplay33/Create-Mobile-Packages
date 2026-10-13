package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.theidler.create_mobile_packages.network_settings.NetworkSettingsHelper.createNetworkSettingsButton;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {

    @Shadow
    StockTickerBlockEntity blockEntity;

    protected StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cmp$addNetworkSettingsButton(CallbackInfo ci) {
        if (blockEntity == null || blockEntity.behaviour == null) return;
        String name = ClientNetworkDataStorage.getVisibleName(blockEntity.behaviour.freqId);
        addRenderableWidget(createNetworkSettingsButton(getGuiLeft() - 22, getGuiTop() + 10, blockEntity.behaviour.freqId, name));
    }
}
