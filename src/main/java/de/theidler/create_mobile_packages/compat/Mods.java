package de.theidler.create_mobile_packages.compat;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.Supplier;

public enum Mods {
    JEI,
    CREATE_FACTORY_LOGISTICS;

    // from com/simibubi/create/compat/Mods.java

    private final String id;

    Mods() {
        id = Lang.asId(name());
    }

    public String id() {
        return id;
    }

    public ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(id, path);
    }

    public Block getBlock(String id) {
        return BuiltInRegistries.BLOCK.get(rl(id));
    }

    public Item getItem(String id) {
        return BuiltInRegistries.ITEM.get(rl(id));
    }

    public boolean contains(ItemLike entry) {
        if (!isLoaded())
            return false;
        Item asItem = entry.asItem();
        return asItem != null && RegisteredObjectsHelper.getKeyOrThrow(asItem)
                .getNamespace()
                .equals(id);
    }

    /**
     * @return a boolean of whether the mod is loaded or not based on mod id
     */
    public boolean isLoaded() {
        return ModList.get().isLoaded(id);
    }

    /**
     * Simple hook to run code if a mod is installed
     * @param toRun will be run only if the mod is loaded
     * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
     */
    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    /**
     * Simple hook to execute code if a mod is installed
     * @param toExecute will be executed only if the mod is loaded
     */
    public void executeIfInstalled(Supplier<Runnable> toExecute) {
        if (isLoaded()) {
            toExecute.get().run();
        }
    }
}
