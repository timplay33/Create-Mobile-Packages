package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.core.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(value = GlobalLogisticsManager.class, remap = false)
public class GlobalLogisticsManagerMixin {

    @Shadow
    public Map<UUID, LogisticsNetwork> logisticsNetworks;

    @Inject(method = "linkAdded", at = @At("RETURN"), remap = false)
    public void create_mobile_packages$linkAdded(UUID networkId, GlobalPos pos, UUID ownedBy, CallbackInfo ci) {
        if (ownedBy == null) return;

        LogisticsNetwork network = logisticsNetworks.get(networkId);
        if (network instanceof IExtendedLogisticsNetwork ext) {
            ext.create_mobile_packages$addPlayer(ownedBy);
        }
    }
}
