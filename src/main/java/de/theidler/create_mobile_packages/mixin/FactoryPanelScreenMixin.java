package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.createmod.catnip.gui.AbstractSimiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static de.theidler.create_mobile_packages.network_settings.NetworkSettingsHelper.createNetworkSettingsButton;

@Mixin(value = FactoryPanelScreen.class, remap = false)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {

    @Shadow
    private FactoryPanelBehaviour behaviour;

    protected FactoryPanelScreenMixin() {
        super();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cmp$addNetworkSettingsButton(CallbackInfo ci) {
        UUID networkId = behaviour == null ? null : behaviour.network;
        if (networkId == null) return;
        String name = ClientNetworkDataStorage.getVisibleName(networkId);
        addRenderableWidget(createNetworkSettingsButton(guiLeft - 22, guiTop - 10, networkId, name));
    }
}
