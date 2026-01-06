package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public class BeePortScreen extends PackagePortScreen {
    private final UUID playerUUID;
    public BeePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        playerUUID = inv.player.getUUID();
    }


    @Override
    protected void init() {
        super.init();
        IconButton addPlayerButton =
                new IconButton(getGuiLeft()-50, getGuiTop()+64, AllIcons.I_ADD);
        addPlayerButton.withCallback(() -> {
            if (menu.contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
                LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(beePortBlockEntity.getLogisticsNetworkId());
                if (network == null) return;
                CreateMobilePackages.IExtendedLogisticsNetwork ext = (CreateMobilePackages.IExtendedLogisticsNetwork) network;
                ext.create_mobile_packages$addPlayer(playerUUID);
            }
        });
        addRenderableWidget(addPlayerButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(graphics, pPartialTick, pMouseX, pMouseY);
        graphics.blit(CreateMobilePackages.asResource("textures/gui/bee_port.png"), getGuiLeft(), getGuiTop(), 0, 47, 220, 82);

        if (menu instanceof BeePortMenu beePortMenu) {
            int eta = beePortMenu.getETA();
            Component text = beePortMenu.isBeeOnTravel()
                    ? Component.translatable("create_mobile_packages.bee_port.screen.arrival_time", eta)
                    : Component.translatable("create_mobile_packages.bee_port.screen.no_bee_on_travel");
            graphics.drawString(font, text, getGuiLeft() + 34, getGuiTop() + 64, 0x3D3C48, false);

            //todo: demo remove
            String out = "error";
            if (menu.contentHolder instanceof BeePortBlockEntity beePortBlockEntity) {
                LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(beePortBlockEntity.getLogisticsNetworkId());
                if (network == null) return;
                CreateMobilePackages.IExtendedLogisticsNetwork ext = (CreateMobilePackages.IExtendedLogisticsNetwork) network;
                out = String.valueOf(ext.create_mobile_packages$getPlayers().size());
            }
            graphics.drawString(font, out, getGuiLeft()-10, getGuiTop()+64, 0xFFFFFF, false);
        }
    }
}
