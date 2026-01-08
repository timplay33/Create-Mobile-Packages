package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerNetworksScreen extends Screen {

    public PlayerNetworksScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        List<IExtendedLogisticsNetwork> networks = getNetworks();
        for (int i = 0; i < networks.size(); i++) {
            IExtendedLogisticsNetwork network = networks.get(i);
            if (!(network instanceof LogisticsNetwork ln)) continue;

            int rowY = 40 + i * 20;
            IconButton leaveBtn = new IconButton(width / 2 + 60, rowY - 4, AllIcons.I_MTD_CLOSE);

            leaveBtn.withCallback(() -> {
                CMPPackets.getChannel().sendToServer(new RemovePlayerFromNetworkPackage(getPlayer().getUUID(), ln.id));
                network.create_mobile_packages$removePlayer(getPlayer().getUUID());
                this.init(minecraft, width, height);
            });

            addRenderableWidget(leaveBtn);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(font, getPlayer().getName(), width / 2, 20, 0xFFFFFF);

        List<IExtendedLogisticsNetwork> networks = getNetworks();
        for (int i = 0; i < networks.size(); i++) {
            guiGraphics.drawString(font, networks.get(i).create_mobile_packages$getName(), width / 2 - 80, 40 + i * 20, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    private List<IExtendedLogisticsNetwork> getNetworks() {
        return Create.LOGISTICS.logisticsNetworks.values().stream()
                .filter(network -> network instanceof IExtendedLogisticsNetwork)
                .map(network -> (IExtendedLogisticsNetwork) network)
                .filter(network -> network.create_mobile_packages$getPlayers().contains(getPlayer().getUUID()))
                .toList();
    }
}
