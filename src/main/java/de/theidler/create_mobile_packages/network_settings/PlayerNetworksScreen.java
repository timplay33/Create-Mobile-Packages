package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.index.CMPGuiTextures;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerNetworksScreen extends Screen {

    private int guiLeft;
    private int guiTop;
    private int windowWidth;
    private int windowHeight;
    private static final int ROW_HEIGHT = 22;
    private final LerpedFloat scroll = LerpedFloat.linear().startWithValue(0);
    private boolean scrollHandleActive;
    private final List<IconButton> networkButtons = new ArrayList<>();
    private IconButton doneBtn;
    private List<UUID> cachedNetworkIds = new ArrayList<>();
    private int lastKnownUpdateCount = -1;

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

        doneBtn = new IconButton(guiLeft + windowWidth - 25, guiTop + windowHeight - 24, AllIcons.I_CONFIRM);
        doneBtn.withCallback(() -> minecraft.setScreen(null));

        CatnipServices.NETWORK.sendToServer(RequestPlayerNetworksPacket.INSTANCE);
        lastKnownUpdateCount = ClientNetworkDataStorage.getUpdateCount();

        refreshNetworks();
    }

    private void refreshNetworks() {
        this.clearWidgets();
        this.networkButtons.clear();
        this.cachedNetworkIds = getNetworks();

        addRenderableWidget(doneBtn);

        for (UUID networkId : cachedNetworkIds) {
            IconButton leaveBtn = new IconButton(0, 0, AllIcons.I_MTD_CLOSE);
            leaveBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.leave"));

            leaveBtn.withCallback(() -> {
                CatnipServices.NETWORK.sendToServer(new RemovePlayerFromNetworkPackage(getPlayer().getUUID(), networkId));
                // Clear cache and refetch networks from server
                ClientNetworkDataStorage.clear();
                CatnipServices.NETWORK.sendToServer(RequestPlayerNetworksPacket.INSTANCE);
                lastKnownUpdateCount = ClientNetworkDataStorage.getUpdateCount();
                this.refreshNetworks();
            });

            addRenderableWidget(leaveBtn);
            networkButtons.add(leaveBtn);

            // Add settings button
            IconButton settingsBtn = new IconButton(0, 0, AllIcons.I_CONFIG_OPEN);
            settingsBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.settings"));
            settingsBtn.withCallback(() -> minecraft.setScreen(new NetworkSettingsScreen(this, networkId)));
            addRenderableWidget(settingsBtn);
            networkButtons.add(settingsBtn);
        }
    }

    @Override
    public void tick() {
        super.tick();
        scroll.tickChaser();

        int currentUpdateCount = ClientNetworkDataStorage.getUpdateCount();
        if (currentUpdateCount != lastKnownUpdateCount) {
            lastKnownUpdateCount = currentUpdateCount;
            refreshNetworks();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float maxScroll = getMaxScroll();
        if (maxScroll <= 0) return false;
        float newTarget = Mth.clamp(scroll.getChaseTarget() - (float) scrollY, 0, maxScroll);
        scroll.chase(newTarget, 0.5f, LerpedFloat.Chaser.EXP);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

    private int getVisibleRows() {
        return (windowHeight - 30) / ROW_HEIGHT;
    }

    private int getMaxScroll() {
        return Math.max(0, cachedNetworkIds.size() + 1 - getVisibleRows());
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = guiLeft;
        int y = guiTop + CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();

        for (int i = 0; i < (windowHeight - CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight()) / CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight(); i++) {
            CMPGuiTextures.PLAYER_NETWORKS_BG.render(graphics, x, y);
            y += CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        float scrollOffset = scroll.getValue(partialTick);
        int listTop = guiTop + 10;
        int listBottom = guiTop + windowHeight - 20;

        guiGraphics.enableScissor(guiLeft, listTop, guiLeft + windowWidth, listBottom);

        Component iconTooltip = null;

        for (int i = 0; i < cachedNetworkIds.size(); i++) {
            float rowTop = listTop + (i + 1 - scrollOffset) * ROW_HEIGHT;
            UUID networkId = cachedNetworkIds.get(i);
            ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);

            int rowCenterY = (int) rowTop + ROW_HEIGHT / 2;

            if (i * 2 + 1 < networkButtons.size()) {
                IconButton leaveBtn = networkButtons.get(i * 2);
                IconButton settingsBtn = networkButtons.get(i * 2 + 1);

                int btnY = rowCenterY - 9;

                leaveBtn.setX(guiLeft + windowWidth - 30);
                leaveBtn.setY(btnY);
                leaveBtn.visible = btnY >= listTop && btnY + 18 <= listBottom;

                settingsBtn.setX(guiLeft + windowWidth - 50);
                settingsBtn.setY(btnY);
                settingsBtn.visible = btnY >= listTop && btnY + 18 <= listBottom;
            }

            if (rowTop + ROW_HEIGHT < listTop || rowTop > listBottom) {
                continue;
            }

            int charBase = rowCenterY - 4;
            boolean isOwner = networkData != null && getPlayer().getUUID().equals(networkData.owner);
            boolean isMember = networkData != null && (networkData.players.contains(getPlayer().getUUID()) || networkData.isOwnerMember);

            int ownerCX = guiLeft + 8;
            guiGraphics.drawString(font, "O", ownerCX, charBase, isOwner ? 0x4A2D31 : 0xB5B0B0, false);

            int memberCX = guiLeft + 22;
            guiGraphics.drawString(font, "M", memberCX, charBase, isMember ? 0x4A2D31 : 0xB5B0B0, false);

            String name = networkData != null ? networkData.name : "Loading...";
            guiGraphics.drawString(font, name, guiLeft + 44, charBase, 0x3D3C48, false);

            int hitTop = rowCenterY - ROW_HEIGHT / 2;
            int hitBot = rowCenterY + ROW_HEIGHT / 2;
            boolean hoveringOwner = isOwner && mouseX >= ownerCX - 2 && mouseX < ownerCX + 14 && mouseY >= hitTop && mouseY < hitBot;
            boolean hoveringMember = isMember && mouseX >= memberCX - 2 && mouseX < memberCX + 14 && mouseY >= hitTop && mouseY < hitBot;

            if (hoveringOwner) {
                iconTooltip = Component.translatable("tooltip.create_mobile_packages.network.owner_indicator");
            } else if (hoveringMember) {
                iconTooltip = Component.translatable("tooltip.create_mobile_packages.network.member_indicator");
            }
        }

        guiGraphics.disableScissor();

        if (iconTooltip != null) {
            guiGraphics.renderTooltip(font, iconTooltip, mouseX, mouseY);
        }

        renderScrollbar(guiGraphics);

        doneBtn.visible = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        doneBtn.visible = true;

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
        int visibleRows = getVisibleRows();
        int barSize = Math.max(10, (int) (barHeight * ((float) visibleRows / (cachedNetworkIds.size() + 1))));
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

    private List<UUID> getNetworks() {
        return ClientNetworkDataStorage.getNetworks().entrySet().stream()
                .filter(entry -> entry.getValue().players.contains(getPlayer().getUUID()) || getPlayer().getUUID().equals(entry.getValue().owner))
                .filter(entry -> !"Unnamed Network".equals(entry.getValue().name))
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Do nothing - don't render the blur background
    }
}
