package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class NetworkSettingsHelper {
    public static IconButton createNetworkSettingsButton(int x, int y, UUID networkId) {
        IconButton settingsButton =
                new IconButton(x, y, AllIcons.I_ADD);

        settingsButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.settings"));

        settingsButton.withCallback(() -> {
            Minecraft.getInstance().setScreen(new NetworkSettingsScreen(Minecraft.getInstance().screen, networkId));
        });

        return settingsButton;
    }

    public static IconButton createNetworkSettingsButton(int x, int y, UUID networkId, String networkName) {
        IconButton settingsButton = createNetworkSettingsButton(x, y, networkId);
        if (networkName != null && !networkName.isBlank()) {
            settingsButton.getToolTip()
                    .add(Component.translatable("tooltip.create_mobile_packages.network.name", networkName)
                            .withStyle(ChatFormatting.GOLD));
        }
        return settingsButton;
    }
}
