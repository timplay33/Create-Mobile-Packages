package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.CMPIcons;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static de.theidler.create_mobile_packages.network_settings.NetworkSettingsHelper.createNetworkSettingsButton;

public class BeePortScreen extends PackagePortScreen {

    private IconButton returnModeButton;
    private IconButton filterModeButton;

    public BeePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = getGuiLeft() - 22;
        int buttonY = getGuiTop() - 10;

        LogisticallyLinkedBehaviour lo = (LogisticallyLinkedBehaviour) menu.contentHolder.getAllBehaviours().stream().filter(b -> b instanceof LogisticallyLinkedBehaviour).findFirst().orElse(null);
        if (lo == null) return;
        addRenderableWidget(createNetworkSettingsButton(buttonX, buttonY, lo.freqId));

        if (!(menu instanceof BeePortMenu beePortMenu) || !(menu.contentHolder instanceof BeePortBlockEntity beePort))
            return;

        createReturnModeButton(buttonX, buttonY, beePort);
        updateReturnModeButton(beePortMenu.isBeeReturnModeEnabled());
        addRenderableWidget(returnModeButton);

        createFilterModeButton(buttonX, buttonY, beePort);
        updateFilterModeButton(beePortMenu.getFilterMode());
        addRenderableWidget(filterModeButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(graphics, pPartialTick, pMouseX, pMouseY);
        graphics.blit(CreateMobilePackages.asResource("textures/gui/bee_port.png"), getGuiLeft(), getGuiTop(), 0, 47, 220, 82);

        if (menu instanceof BeePortMenu beePortMenu) {
            updateReturnModeButton(beePortMenu.isBeeReturnModeEnabled());
            updateFilterModeButton(beePortMenu.getFilterMode());
            int eta = beePortMenu.getETA();
            Component text = beePortMenu.isBeeOnTravel()
                    ? Component.translatable("create_mobile_packages.bee_port.screen.arrival_time", eta)
                    : Component.translatable("create_mobile_packages.bee_port.screen.no_bee_on_travel");
            graphics.drawString(font, text, getGuiLeft() + 34, getGuiTop() + 64, 0x3D3C48, false);
        }
    }

    private void createReturnModeButton(int buttonX, int buttonY, BeePortBlockEntity beePort) {
        returnModeButton = new IconButton(buttonX, buttonY + 18 + 18, CMPIcons.I_RETURN);
        returnModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.enable_return_mode"));
        returnModeButton.withCallback(() -> CatnipServices.NETWORK.sendToServer(
                new ToggleBeeReturnModePacket(beePort.getBlockPos())
        ));
    }

    private void updateReturnModeButton(boolean returnModeEnabled) {
        if (returnModeButton == null) return;

        if (returnModeEnabled) {
            returnModeButton.setIcon(CMPIcons.I_DIRECT);
            returnModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.disable_return_mode"));
        } else {
            returnModeButton.setIcon(CMPIcons.I_RETURN);
            returnModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.enable_return_mode"));
        }
        returnModeButton.getToolTip().add(Component.translatable("tooltip.create_mobile_packages.bee_port.return_mode.description").withStyle(ChatFormatting.GRAY));
    }

    private void createFilterModeButton(int buttonX, int buttonY, BeePortBlockEntity beePort) {
        filterModeButton = new IconButton(buttonX, buttonY + 18, CMPIcons.I_ROBO_BEE_AND_PACKAGE);
        filterModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.filter_mode.all"));
        filterModeButton.withCallback(() -> CatnipServices.NETWORK.sendToServer(
                new ToggleFilterModePacket(beePort.getBlockPos())
        ));
    }

    private void updateFilterModeButton(FilterMode filterMode) {
        Component description = Component.translatable("tooltip.create_mobile_packages.bee_port.filter_mode.description").withStyle(ChatFormatting.GRAY);
        switch (filterMode) {
            case ROBO_ONLY:
                filterModeButton.setIcon(CMPIcons.I_ROBO_BEE);
                filterModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.filter_mode.bees"));
                break;
            case PACKAGES_ONLY:
                filterModeButton.setIcon(CMPIcons.I_PACKAGE);
                filterModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.filter_mode.packages"));
                break;
            default:
                filterModeButton.setIcon(CMPIcons.I_ROBO_BEE_AND_PACKAGE);
                filterModeButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.bee_port.filter_mode.all"));
                break;
        }
        filterModeButton.getToolTip().add(description);
    }
}
