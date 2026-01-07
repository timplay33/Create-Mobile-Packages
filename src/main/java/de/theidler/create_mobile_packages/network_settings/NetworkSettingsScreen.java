package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class NetworkSettingsScreen extends Screen {

    private final UUID  networkId;
    private final Screen parent;
    private LogisticsNetwork network;
    private IExtendedLogisticsNetwork extendedNetwork;
    private EditBox nameBox;
    private IconButton addPlayerButton;

    protected NetworkSettingsScreen(@Nullable Screen parent, UUID networkId) {
        super(Component.literal(networkId.toString()));
        this.parent = parent;
        this.networkId = networkId;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        extendedNetwork = (IExtendedLogisticsNetwork) network;
        createNameBox();
    }
    private void createNameBox() {
        Consumer<String> onTextChanged = s -> nameBox.setX(nameBoxX(s, nameBox));
        nameBox = new EditBox(new NoShadowFontWrapper(font), 23, 20, width -20, 10, Component.empty());
        nameBox.setMaxLength(25);
        nameBox.setBordered(false);
        nameBox.setValue(extendedNetwork.create_mobile_packages$getName());
        nameBox.setResponder(onTextChanged);
        nameBox.setX(nameBoxX(nameBox.getValue(), nameBox));
        addRenderableWidget(nameBox);
    }

    private int nameBoxX(String s, EditBox nameBox) {
        return width / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        //save network name
        CMPPackets.getChannel().sendToServer(new SetNetworkNamePackage(nameBox.getValue(), networkId));

        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

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

