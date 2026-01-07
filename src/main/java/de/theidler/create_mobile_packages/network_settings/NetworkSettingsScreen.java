package de.theidler.create_mobile_packages.network_settings;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NetworkSettingsScreen extends Screen {

    private final UUID  networkId;
    private final Screen parent;
    private LogisticsNetwork network;
    private IExtendedLogisticsNetwork extendedNetwork;

    protected NetworkSettingsScreen(@Nullable Screen parent, UUID networkId) {
        super(Component.literal(networkId.toString()));
        this.parent = parent;
        this.networkId = networkId;
    }

    @Override
    protected void init() {
        network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        extendedNetwork = (IExtendedLogisticsNetwork) network;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(font, extendedNetwork.create_mobile_packages$getName(), this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(font, "Network ID: " + networkId.toString(), 10, 64, 0xFFFFFF, false);

        guiGraphics.drawString(font, "Owner ID: " + getPlayerName(network.owner), 10, 84, 0xFFFFFF, false);

        guiGraphics.drawString(font, "Locked: " + network.locked, 10, 104, 0xFFFFFF, false);

        List<UUID> players = extendedNetwork.create_mobile_packages$getPlayers().stream().toList();
        for (int i = 0; i < players.size(); i++) {
            guiGraphics.drawString(font, getPlayerName(players.get(i)), 10, 124 + 20*i, 0xFFFFFF, false);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public String getPlayerName(UUID uuid) {
        if (uuid == null) return "";
        if (minecraft == null || minecraft.level == null) return "";
        Player player = minecraft.level.getPlayerByUUID(uuid);
        if (player == null) return "";
        return player.getName().getString();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }
}

