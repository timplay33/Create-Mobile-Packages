package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPGuiTextures;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerNetworksScreen extends Screen {

    private int guiLeft;
    private int guiTop;
    private int windowWidth;
    private int windowHeight;
    private final LerpedFloat scroll = LerpedFloat.linear().startWithValue(0);
    private boolean scrollHandleActive;
    private final List<IconButton> networkButtons = new ArrayList<>();
    private IconButton doneBtn;

    public PlayerNetworksScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int appropriateHeight = Minecraft.getInstance()
                .getWindow()
                .getGuiScaledHeight() - 10;
        appropriateHeight -=
                Mth.positiveModulo(appropriateHeight - CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight(), CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight());
        appropriateHeight =
                Math.min(appropriateHeight, CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() + CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight() + CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight() * 17);

        windowWidth = 210;
        windowHeight = appropriateHeight;
        super.init();
        this.guiLeft = (width - windowWidth) / 2;
        this.guiTop = (height - windowHeight) / 2;
        refreshNetworks();

        doneBtn = new IconButton(guiLeft + windowWidth - 25, guiTop + windowHeight - 24, AllIcons.I_CONFIRM);
        doneBtn.withCallback(() -> minecraft.setScreen(null));
    }

    private void refreshNetworks() {
        this.clearWidgets();
        this.networkButtons.clear();

        List<IExtendedLogisticsNetwork> networks = getNetworks();
        for (IExtendedLogisticsNetwork network : networks) {
            if (!(network instanceof LogisticsNetwork ln)) continue;

            IconButton leaveBtn = new IconButton(0, 0, AllIcons.I_MTD_CLOSE);
            leaveBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.leave"));

            leaveBtn.withCallback(() -> {
                CMPPackets.getChannel().sendToServer(new RemovePlayerFromNetworkPackage(getPlayer().getUUID(), ln.id));
                network.create_mobile_packages$removePlayer(getPlayer().getUUID());
                this.refreshNetworks();
            });

            addRenderableWidget(leaveBtn);
            networkButtons.add(leaveBtn);

            // Add settings button
            IconButton settingsBtn = new IconButton(0, 0, AllIcons.I_CONFIG_OPEN);
            settingsBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.settings"));
            settingsBtn.withCallback(() -> minecraft.setScreen(new NetworkSettingsScreen(this, ln.id)));
            addRenderableWidget(settingsBtn);
            networkButtons.add(settingsBtn);
        }
    }

    @Override
    public void tick() {
        super.tick();
        scroll.tickChaser();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float maxScroll = getMaxScroll();
        if (maxScroll <= 0) return false;
        float newTarget = Mth.clamp(scroll.getChaseTarget() - (float) delta, 0, maxScroll);
        scroll.chase(newTarget, 0.5f, LerpedFloat.Chaser.EXP);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (doneBtn.isMouseOver(mouseX, mouseY)) {
            doneBtn.onClick(mouseX, mouseY);
            return true;
        }
        int maxScroll = getMaxScroll();
        if (maxScroll > 0 && button == 0) {
            int barX = guiLeft + windowWidth - 8;
            int barY = guiTop + 15;
            int barWidth = 6;
            int barHeight = windowHeight - 35;
            if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
                scrollHandleActive = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            scrollHandleActive = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollHandleActive && button == 0) {
            int maxScroll = getMaxScroll();
            int barHeight = windowHeight - 35;
            
            double relativeY = mouseY - (guiTop + 15);
            float target = (float) (relativeY / barHeight * maxScroll);
            scroll.chase(Mth.clamp(target, 0, maxScroll), 0.5f, LerpedFloat.Chaser.EXP);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private int getMaxScroll() {
        return Math.max(0, getNetworks().size() + 1 - 5);
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = guiLeft;
        int y = guiTop;

        CMPGuiTextures.PLAYER_NETWORKS_HEADER.render(graphics, x, y);
        y += CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();
        for (int i = 0; i < (windowHeight - CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight()) / CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight(); i++) {
            CMPGuiTextures.PLAYER_NETWORKS_BG.render(graphics, x, y);
            y += CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        }
        CMPGuiTextures.PLAYER_NETWORKS_FOOTER.render(graphics, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        float scrollOffset = scroll.getValue(partialTick);
        int listTop = guiTop + 10;
        int listBottom = guiTop + windowHeight - 20;

        guiGraphics.enableScissor(guiLeft, listTop, guiLeft + windowWidth, listBottom);

        List<IExtendedLogisticsNetwork> networks = getNetworks();
        for (int i = 0; i < networks.size(); i++) {
            float rowY = listTop + (i + 1 - scrollOffset) * 20;

            if (i * 2 + 1 < networkButtons.size()) {
                IconButton leaveBtn = networkButtons.get(i * 2);
                IconButton settingsBtn = networkButtons.get(i * 2 + 1);

                int btnY = (int) rowY - 4;
                boolean visible = rowY - 4 >= listTop && rowY + 16 <= listBottom;

                leaveBtn.setX(guiLeft + windowWidth - 30);
                leaveBtn.setY(btnY);
                leaveBtn.visible = visible;

                settingsBtn.setX(guiLeft + windowWidth - 50);
                settingsBtn.setY(btnY);
                settingsBtn.visible = visible;
            }

            if (rowY < listTop || rowY > listBottom) {
                continue;
            }

            guiGraphics.drawString(font, networks.get(i).create_mobile_packages$getName(), guiLeft + 20, (int) rowY, 0x3D3C48, false);
        }

        guiGraphics.disableScissor();

        renderScrollbar(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render header and footer again to be above buttons
        CMPGuiTextures.PLAYER_NETWORKS_HEADER.render(guiGraphics, guiLeft, guiTop);
        CMPGuiTextures.PLAYER_NETWORKS_FOOTER.render(guiGraphics, guiLeft, guiTop + windowHeight - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight());

        // render done button after footer to show on top
        doneBtn.doRender(guiGraphics, mouseX, mouseY, partialTick);

        String text = getTitle().getString();
        guiGraphics.drawString(font, text, guiLeft + windowWidth / 2 - font.width(text) / 2, guiTop + 4, 0x4A2D31, false);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        int barX = guiLeft + windowWidth - 8;
        int barY = guiTop + 15;
        int barHeight = windowHeight - 35;

        float scrollOffset = scroll.getValue();
        int barSize = Math.max(10, (int) (barHeight * (5f / (getNetworks().size() + 1))));
        int scrollBarY = barY + (int) ((barHeight - barSize) * (scrollOffset / maxScroll));

        AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
        guiGraphics.blit(pad.location, barX, barY, pad.getWidth(), barHeight, pad.getStartX(), pad.getStartY(),
                pad.getWidth(), pad.getHeight(), 256, 256);

        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(guiGraphics, barX, scrollBarY);
        if (barSize > 16)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(guiGraphics, barX, scrollBarY + barSize / 2 - 4);
        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(guiGraphics, barX, scrollBarY + barSize - 5);
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
