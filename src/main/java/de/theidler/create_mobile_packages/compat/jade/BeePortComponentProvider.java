package de.theidler.create_mobile_packages.compat.jade;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
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
            iTooltip.add(Component.translatable("tooltip.create_mobile_packages.bee_port.jade.network", blockAccessor.getServerData().getString("Network"))); //"Network: " + ));
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
        BeePortBlockEntity bpbe = (BeePortBlockEntity) blockAccessor.getBlockEntity();
        LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(bpbe.getLogisticsNetworkId());
        if (network == null) return;
        IExtendedLogisticsNetwork extendedNetwork = (IExtendedLogisticsNetwork) network;
        compoundTag.putString("Network", extendedNetwork.create_mobile_packages$getName());
        Player player = blockAccessor.getPlayer();
        if (extendedNetwork.create_mobile_packages$getPlayers().contains(player.getUUID())) {
            compoundTag.putBoolean("IsPart", true);
        }
    }
}
