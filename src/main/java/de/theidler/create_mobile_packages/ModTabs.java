package de.theidler.create_mobile_packages;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobilePackages.MODID);

    public static final RegistryObject<CreativeModeTab> CREATE_MOBILE_PACKAGES_TAB = CREATIVE_MODE_TABS.register("create_mobile_packages_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.create_mobile_packages"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.DRONE_PORT_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.DRONE_PORT_BLOCK_ITEM.get());
            }).build());
}
