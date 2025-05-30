package de.theidler.create_mobile_packages;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;

public record Location(BlockPos position, DimensionType dimensionType) {
}
