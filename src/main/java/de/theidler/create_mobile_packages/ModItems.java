package de.theidler.create_mobile_packages;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreateMobilePackages.MODID);

    public static final RegistryObject<Item> DRONE_PORT_BLOCK_ITEM = ITEMS.register("drone_port",
            () -> new BlockItem(ModBlocks.DRONE_PORT_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(ModBlocks.EXAMPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));
}
