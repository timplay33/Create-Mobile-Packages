package de.theidler.create_mobile_packages.blocks.drone_port;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    public static final Capability<IDronePortEntityTracker> DRONE_PORT_ENTITY_TRACKER_CAP = CapabilityManager.get(new CapabilityToken<IDronePortEntityTracker>(){});

    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(IDronePortEntityTracker.class);
    }
}

