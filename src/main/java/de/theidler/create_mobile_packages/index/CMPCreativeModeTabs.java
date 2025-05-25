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
            .icon(() -> CMPBlocks.BEE_PORT.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.create_mobile_packages"))
            .displayItems((itemDisplayParameters, output) -> CreateMobilePackages.REGISTRATE.getAll(Registries.ITEM).forEach((item -> {
                output.accept(item.get());
            })))
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        private List<Item> collectBlocks(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Block> entry : CreateMobilePackages.REGISTRATE.getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB))
                    continue;
                Item item = entry.get()
                        .asItem();
                if (item == Items.AIR)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
            return items;
        }

        private List<Item> collectItems(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();


            for (RegistryEntry<Item> entry : CreateMobilePackages.REGISTRATE.getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, CMPCreativeModeTabs.CREATE_MOBILE_PACKAGES_TAB))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }

        private static void outputAll(CreativeModeTab.Output output, List<Item> items) {
            for (Item item : items) {
                output.accept(item);
            }
        }

        List<Item> exclude = List.of(CMPItems.DRONE_CONTROLLER.get(), CMPBlocks.DRONE_PORT.get().asItem());

        @Override
        public void accept(CreativeModeTab.@NotNull ItemDisplayParameters pParameters, CreativeModeTab.@NotNull Output pOutput) {
            List<Item> items = new LinkedList<>();
            items.addAll(collectBlocks((item) -> exclude.contains(item)));
            items.addAll(collectItems((item) -> exclude.contains(item)));

            outputAll(pOutput, items);
        }
    }
}
