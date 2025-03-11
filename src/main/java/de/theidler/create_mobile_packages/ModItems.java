package de.theidler.create_mobile_packages;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreateMobilePackages.MODID);

    public static final RegistryObject<Item> DRONE_PORT_BLOCK_ITEM = ITEMS.register("drone_port",
            () -> new BlockItem(ModBlocks.DRONE_PORT_BLOCK.get(), new Item.Properties()));
}
