package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static de.theidler.create_mobile_packages.network_settings.NetworkSettingsHelper.createNetworkSettingsButton;

@Mixin(value = RedstoneRequesterScreen.class, remap = false)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {

    protected RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cmp$addNetworkSettingsButton(CallbackInfo ci) {
        if (menu == null || menu.contentHolder == null || menu.contentHolder.behaviour == null) return;
        UUID freqId = menu.contentHolder.behaviour.freqId;
        String name = ClientNetworkDataStorage.getVisibleName(freqId);
        addRenderableWidget(createNetworkSettingsButton(getGuiLeft() - 22, getGuiTop() - 10, freqId, name));
    }
}
