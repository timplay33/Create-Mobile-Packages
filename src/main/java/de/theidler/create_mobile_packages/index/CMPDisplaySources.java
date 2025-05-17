package de.theidler.create_mobile_packages.index;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import de.theidler.create_mobile_packages.blocks.bee_port.BeeCountDisplaySource;
import de.theidler.create_mobile_packages.blocks.bee_port.BeeOnTravelETADisplaySource;

import java.util.function.Supplier;

import static de.theidler.create_mobile_packages.CreateMobilePackages.REGISTRATE;

public class CMPDisplaySources {
    public static final RegistryEntry<BeeCountDisplaySource> BEE_COUNT = simple("bee_count" , BeeCountDisplaySource::new);
    public static final RegistryEntry<BeeOnTravelETADisplaySource> BEE_ETA = simple("bee_eta" , BeeOnTravelETADisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<T> simple(String name, Supplier<T> supplier) {
        return REGISTRATE.displaySource(name, supplier).register();
    }

    public static void register() {
    }
}