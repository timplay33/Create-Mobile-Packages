package de.theidler.create_mobile_packages;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record Location(BlockPos position, Level level) {
}
