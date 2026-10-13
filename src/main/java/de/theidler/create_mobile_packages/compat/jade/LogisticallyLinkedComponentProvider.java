package de.theidler.create_mobile_packages.compat.jade;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

public enum LogisticallyLinkedComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        UUID networkId = getNetworkId(accessor.getBlockEntity());
        if (networkId == null) return;
        String name = ClientNetworkDataStorage.getVisibleName(networkId);
        if (name == null || name.isBlank()) return;
        tooltip.add(Component.translatable("tooltip.create_mobile_packages.network.name", name));
    }

    private static UUID getNetworkId(BlockEntity blockEntity) {
        if (blockEntity instanceof BeePortBlockEntity) return null;
        LogisticallyLinkedBehaviour behaviour =
                BlockEntityBehaviour.get(blockEntity, LogisticallyLinkedBehaviour.TYPE);
        return behaviour == null ? null : behaviour.freqId;
    }

    @Override
    public ResourceLocation getUid() {
        return CreateMobilePackages.asResource("logistically_linked");
    }
}
