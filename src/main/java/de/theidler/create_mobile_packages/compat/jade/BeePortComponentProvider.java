package de.theidler.create_mobile_packages.compat.jade;

import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import de.theidler.create_mobile_packages.network_settings.NetworkHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BeePortComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("Network")) {
            iTooltip.add(Component.translatable("tooltip.create_mobile_packages.bee_port.jade.network", blockAccessor.getServerData().getString("Network")));
        }
        if (blockAccessor.getServerData().contains("IsPart")) {
            iTooltip.add(Component.translatable("tooltip.create_mobile_packages.bee_port.jade.part_of_network"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return de.theidler.create_mobile_packages.CreateMobilePackages.asResource("bee_port");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof BeePortBlockEntity beePortBlockEntity) {
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(beePortBlockEntity.getLogisticsNetworkId());
            if (extendedNetwork == null) return;
            compoundTag.putString("Network", extendedNetwork.create_mobile_packages$getName());
            Player player = blockAccessor.getPlayer();
            if (extendedNetwork.create_mobile_packages$isPlayerMember(player.getUUID())) {
                compoundTag.putBoolean("IsPart", true);
            }
        }
    }
}
