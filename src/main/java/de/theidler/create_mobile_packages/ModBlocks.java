package de.theidler.create_mobile_packages;

import de.theidler.create_mobile_packages.blocks.DronePortBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreateMobilePackages.MODID);

    public static final RegistryObject<Block> DRONE_PORT_BLOCK = BLOCKS.register("drone_port",
            () -> new DronePortBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
}
