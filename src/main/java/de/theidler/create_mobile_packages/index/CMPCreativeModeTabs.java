package de.theidler.create_mobile_packages.index;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CMPCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobilePackages.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_MOBILE_PACKAGES_TAB = CREATIVE_MODE_TABS.register("create_mobile_packages_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_mobile_packages"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(CMPBlocks.DRONE_PORT::asStack)
                    .displayItems(new RegistrateDisplayItemsGenerator(true, CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB))
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        private final boolean addItems;
        private final DeferredHolder<CreativeModeTab, CreativeModeTab> tabFilter;

        public RegistrateDisplayItemsGenerator(boolean addItems, DeferredHolder<CreativeModeTab, CreativeModeTab> tabFilter) {
            this.addItems = addItems;
            this.tabFilter = tabFilter;
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {

        }
    }
}
