package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class NetworkSettingsHelper {
    public static IconButton createNetworkSettingsButton(int x, int y, UUID networkId) {
        IconButton settingsButton =
                new IconButton(x, y, AllIcons.I_ADD);

        settingsButton.withCallback(() -> {
            Minecraft.getInstance().setScreen(new NetworkSettingsScreen(Minecraft.getInstance().screen, networkId));
        });

        return settingsButton;
    }
}
