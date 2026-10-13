package de.theidler.create_mobile_packages.compat.jade;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public enum FactoryPanelComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof FactoryPanelBlockEntity panelBE)) return;

        Set<String> names = new LinkedHashSet<>();
        for (FactoryPanelBehaviour behaviour : panelBE.panels.values()) {
            if (behaviour == null || !behaviour.isActive()) continue;
            UUID networkId = behaviour.network;
            if (networkId == null) continue;
            String name = ClientNetworkDataStorage.getVisibleName(networkId);
            if (name == null || name.isBlank()) continue;
            names.add(name);
        }

        for (String name : names) {
            tooltip.add(Component.translatable("tooltip.create_mobile_packages.network.name", name));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CreateMobilePackages.asResource("factory_panel");
    }
}
