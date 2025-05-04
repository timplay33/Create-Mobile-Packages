package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CapabilityAttacher {
    private static final ResourceLocation TRACKER_ID = CreateMobilePackages.asResource("dpe_tracker");

    @SubscribeEvent
    public static void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        // Only attach on the server side
        if (!event.getObject().isClientSide()) {
            DronePortEntityTracker tracker = new DronePortEntityTracker();
            event.addCapability(TRACKER_ID, new ICapabilityProvider() {
                private final LazyOptional<IDronePortEntityTracker> opt = LazyOptional.of(() -> tracker);

                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                    return cap == ModCapabilities.DRONE_PORT_ENTITY_TRACKER_CAP ? opt.cast() : LazyOptional.empty();
                }
            });
        }
    }
}

