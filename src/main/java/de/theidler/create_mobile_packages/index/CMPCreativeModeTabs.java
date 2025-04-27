package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CMPCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobilePackages.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_MOBILE_PACKAGES_TAB = CREATIVE_MODE_TABS.register("create_mobile_packages_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> CMPBlocks.DRONE_PORT.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.create_mobile_packages"))
            .displayItems((itemDisplayParameters, output) -> CreateMobilePackages.REGISTRATE.getAll(Registries.ITEM).forEach((item -> {
                output.accept(item.get());
            })))
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
