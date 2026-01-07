package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static de.theidler.create_mobile_packages.network_settings.NetworkSettingsHelper.createNetworkSettingsButton;

public class BeePortScreen extends PackagePortScreen {

    public BeePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        LogisticallyLinkedBehaviour lo = (LogisticallyLinkedBehaviour) menu.contentHolder.getAllBehaviours().stream().filter(b -> b instanceof LogisticallyLinkedBehaviour).findFirst().orElse(null);
        if (lo == null) return;
        addRenderableWidget(createNetworkSettingsButton(getGuiLeft() + 180, getGuiTop() - 12, lo.freqId));
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
        }
    }
}
