package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.index.CMPGuiTextures;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class NetworkSettingsScreen extends Screen {

    private final UUID networkId;
    private final Screen parent;
    private EditBox nameBox;
    private IconButton addPlayerButton;
    private IconButton networkLockButton;
    private final List<IconButton> playerButtons = new ArrayList<>();
    private final LerpedFloat scroll = LerpedFloat.linear().startWithValue(0);
    private boolean scrollHandleActive;
    private final int windowWidth = 210;

    private int guiLeft;
    private int guiTop;
    private final int windowHeight = 176;
    private IconButton doneBtn;

    protected NetworkSettingsScreen(@Nullable Screen parent, UUID networkId) {
        super(Component.literal(networkId.toString()));
        this.parent = parent;
        this.networkId = networkId;
    }

    private int loadTicks = 0;
    private int lastKnownPlayerCount = -1;
    private String lastKnownNetworkName = null;

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (width - windowWidth) / 2;
        this.guiTop = (height - windowHeight) / 2;

        // Request network data from server
        CatnipServices.NETWORK.sendToServer(new RequestNetworkDataPacket(networkId));

        // Also try to load from server-side data as fallback
        loadFromServerIfNeeded();
    }

    private void refreshUI() {
        this.clearWidgets();
        this.playerButtons.clear();

        ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);
        if (networkData == null) {
            // Data not yet received from server, wait
            return;
        }

        // Update tracking variables
        lastKnownPlayerCount = getEffectivePlayerCount(networkData);
        lastKnownNetworkName = networkData.name;

        createNameBox(networkData);
        createLockButton(networkData);
        createPlayerList(networkData);
        createAddPlayerButton();

        doneBtn = new IconButton(guiLeft + windowWidth - 25, guiTop + windowHeight - 43, AllIcons.I_CONFIRM);
        doneBtn.withCallback(() -> minecraft.setScreen(parent));
        addRenderableWidget(doneBtn);
    }

    private void loadFromServerIfNeeded() {
        // Check for errors first
        if (ClientNetworkDataStorage.hasError(networkId)) {
            // Error was received, no more retries needed
            loadTicks = -1;
            return;
        }

        // Try loading from server data if client cache is empty
        if (ClientNetworkDataStorage.getNetworkData(networkId) == null) {
            loadTicks++;
            if (loadTicks == 1 || loadTicks == 40 || loadTicks == 100) {
                // Request immediately, after 2 seconds, and after 5 seconds
                CatnipServices.NETWORK.sendToServer(new RequestNetworkDataPacket(networkId));
            }
        } else {
            loadTicks = 0;
        }
    }


    private void createLockButton(ClientNetworkDataStorage.NetworkData networkData) {
        networkLockButton = new IconButton(guiLeft + windowWidth - 30, guiTop + 25, networkData.locked ? AllIcons.I_CONFIG_UNLOCKED : AllIcons.I_CONFIG_LOCKED);
        networkLockButton.setToolTip(Component.translatable(networkData.locked ? "create.gui.stock_keeper.network_locked" : "create.gui.stock_keeper.network_open"));
        networkLockButton.withCallback(() -> {
            CatnipServices.NETWORK.sendToServer(new ModifyNetworkLockStatePackage(!networkData.locked, networkId));
            // Server will send back updated data - no need to update locally
        });
        addRenderableWidget(networkLockButton);
    }

    private void createPlayerList(ClientNetworkDataStorage.NetworkData networkData) {
        List<UUID> players = getEffectivePlayers(networkData);
        for (int i = 0; i < players.size(); i++) {
            UUID pId = players.get(i);

            IconButton removeBtn = new IconButton(0, 0, AllIcons.I_MTD_CLOSE);
            removeBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.remove_player"));
            removeBtn.withCallback(() -> {
                CatnipServices.NETWORK.sendToServer(new RemovePlayerFromNetworkPackage(pId, networkId));
                // Server will send back updated data - no need to update locally
            });
            addRenderableWidget(removeBtn);
            playerButtons.add(removeBtn);
        }
    }

    private void createAddPlayerButton() {
        addPlayerButton = new IconButton(guiLeft + windowWidth - 50, guiTop + 25, AllIcons.I_ADD);
        addPlayerButton.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.add_yourself"));
        addPlayerButton.withCallback(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            CatnipServices.NETWORK.sendToServer(new AddPlayerToNetworkPackage(player.getUUID(), networkId));
            // Server will send back updated data - no need to update locally
        });
        addRenderableWidget(addPlayerButton);
    }

    private void createNameBox(ClientNetworkDataStorage.NetworkData networkData) {
        Consumer<String> onTextChanged = s -> {
            nameBox.setX(nameBoxX(s, nameBox));
            //save network name
            CatnipServices.NETWORK.sendToServer(new SetNetworkNamePackage(nameBox.getValue(), networkId));
            networkData.name = nameBox.getValue();
        };
        nameBox = new EditBox(new NoShadowFontWrapper(font), guiLeft + 25, guiTop + 4, windowWidth - 50, 10, Component.empty());
        nameBox.setMaxLength(25);
        nameBox.setBordered(false);
        nameBox.setValue(networkData.name);
        nameBox.setResponder(onTextChanged);
        nameBox.setX(nameBoxX(nameBox.getValue(), nameBox));
        nameBox.setTextColor(0x4A2D31);
        addRenderableWidget(nameBox);
    }

    private int nameBoxX(String s, EditBox nameBox) {
        return guiLeft + windowWidth / 2 - (Math.min(font.width(s), nameBox.getWidth())) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        scroll.tickChaser();
        loadFromServerIfNeeded();

        // Check if network data changed and refresh UI if needed
        ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);
        if (networkData != null && nameBox != null) { // Only check if UI is initialized
            boolean dataChanged = lastKnownPlayerCount != getEffectivePlayerCount(networkData);

            // Check if player count changed

            // Check if name changed (excluding our own edits)
            if (lastKnownNetworkName != null && !lastKnownNetworkName.equals(networkData.name) && !nameBox.isFocused()) {
                dataChanged = true;
            }

            if (dataChanged) {
                refreshUI();
            }
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
        // If error is shown, close on click
        if (ClientNetworkDataStorage.hasError(networkId)) {
            minecraft.setScreen(parent);
            return true;
        }

        if (doneBtn != null && doneBtn.isMouseOver(mouseX, mouseY)) {
            doneBtn.onClick(mouseX, mouseY);
            return true;
        }
        if (nameBox != null && !nameBox.isFocused()) {
            String text = nameBox.getValue();
            int iconX = nameBoxX(text, nameBox) + font.width(text) + 5;
            int iconY = guiTop + 1;
            if (mouseX >= iconX && mouseX <= iconX + 13 && mouseY >= iconY && mouseY <= iconY + 13) {
                nameBox.setFocused(true);
                setFocused(nameBox);
                nameBox.setCursorPosition(nameBox.getValue().length());
                return true;
            }
        }
        int maxScroll = getMaxScroll();
        if (maxScroll > 0 && button == 0) {
            int barX = guiLeft + windowWidth - 8;
            int barY = guiTop + 15;
            int barWidth = 6;
            int barHeight = getScrollbarHeight();
            if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
                scrollHandleActive = true;
                return true;
            }
        }
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!result && nameBox != null && nameBox.isFocused()) {
            nameBox.setFocused(false);
        }
        return result;
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
            int barHeight = getScrollbarHeight();
            double relativeY = mouseY - (guiTop + 15);
            float target = (float) (relativeY / barHeight * maxScroll);
            scroll.chase(Mth.clamp(target, 0, maxScroll), 0.5f, LerpedFloat.Chaser.EXP);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private int getVisibleRows() {
        int hH = CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();
        int bgH = CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        int fH = CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight();
        int bgCount = (windowHeight - hH - fH) / bgH;
        int contentH = hH + bgCount * bgH - 15;
        return Math.max(1, contentH / 20);
    }

    private int getMaxScroll() {
        ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);
        if (networkData == null) return 0;
        return Math.max(0, getEffectivePlayerCount(networkData) + 2 - getVisibleRows());
    }

    @Override
    public void onClose() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            super.onClose();
        }
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int y = guiTop;

        CMPGuiTextures.PLAYER_NETWORKS_HEADER.render(graphics, guiLeft, y);
        y += CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();

        for (int i = 0; i < (windowHeight - CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight()) / CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight(); i++) {
            CMPGuiTextures.PLAYER_NETWORKS_BG.render(graphics, guiLeft, y);
            y += CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        }

        CMPGuiTextures.PLAYER_NETWORKS_FOOTER.render(graphics, guiLeft, y);

        if (nameBox != null) {
            String text = nameBox.getValue();
            nameBox.visible = nameBox.isFocused();
            if (!nameBox.isFocused()) {
                graphics.drawString(font, text, nameBoxX(text, nameBox), guiTop + 4, 0x4A2D31, false);
                CMPGuiTextures.PLAYER_NETWORKS_EDIT_NAME.render(graphics, nameBoxX(text, nameBox) + font.width(text) + 5, guiTop + 1);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Check for errors first
        if (ClientNetworkDataStorage.hasError(networkId)) {
            String errorMsg = ClientNetworkDataStorage.getErrorMessage(networkId);
            guiGraphics.drawCenteredString(font, Component.literal(errorMsg), guiLeft + windowWidth / 2, guiTop + windowHeight / 2 - 20, 0xFF5555);
            guiGraphics.drawCenteredString(font, Component.literal("Click to close"), guiLeft + windowWidth / 2, guiTop + windowHeight / 2 + 10, 0xFFFFFF);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);
        if (networkData == null) {
            // Data not loaded yet - display loading message or wait
            if (nameBox == null) {
                guiGraphics.drawCenteredString(font, Component.literal("Loading..."), guiLeft + windowWidth / 2, guiTop + windowHeight / 2, 0xFFFFFF);
            }
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        // If data just arrived and UI hasn't been initialized, do it now
        if (nameBox == null) {
            refreshUI();
        }

        float scrollOffset = scroll.getValue(partialTick);
        int contentTop = guiTop + 15;
        int headerH = CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();
        int bgH = CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        int footerH = CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight();
        int bgCount = (windowHeight - headerH - footerH) / bgH;
        int footerY = guiTop + headerH + bgCount * bgH;
        int contentBottom = footerY;

        guiGraphics.enableScissor(guiLeft, contentTop, Integer.MAX_VALUE, contentBottom);

        int scrollRowOffset = (int) (scrollOffset * 20);

        if (networkLockButton != null)
            networkLockButton.setY(guiTop + 25 - scrollRowOffset);
        if (addPlayerButton != null)
            addPlayerButton.setY(guiTop + 25 - scrollRowOffset);

        guiGraphics.drawString(font, Component.translatable("create_mobile_packages.network.owner", getPlayerName(networkData.owner)), guiLeft + 20, guiTop + 30 - scrollRowOffset, 0x3D3C48, false);

        guiGraphics.drawString(font, Component.translatable("create_mobile_packages.network.players"), guiLeft + 20, guiTop + 50 - scrollRowOffset, 0x3D3C48, false);

        List<UUID> effectivePlayers = getEffectivePlayers(networkData);
        for (int i = 0; i < effectivePlayers.size(); i++) {
            float rowY = guiTop + 65 + (i - scrollOffset) * 20;

            if (rowY + 15 < contentTop || rowY > contentBottom) {
                if (i < playerButtons.size()) {
                    playerButtons.get(i).visible = false;
                }
                continue;
            }

            guiGraphics.drawString(font, getPlayerName(effectivePlayers.get(i)), guiLeft + 30, (int) rowY, 0x555555, false);

            if (i < playerButtons.size()) {
                IconButton removeBtn = playerButtons.get(i);
                removeBtn.setX(guiLeft + windowWidth - 30);
                removeBtn.setY((int) rowY - 5);
                removeBtn.visible = true;
            }
        }

        if (doneBtn != null)
            doneBtn.visible = false;
        boolean nameBoxFocused = nameBox != null && nameBox.isFocused();
        if (nameBox != null)
            nameBox.visible = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (nameBox != null)
            nameBox.visible = nameBoxFocused;
        if (doneBtn != null)
            doneBtn.visible = true;

        guiGraphics.disableScissor();

        renderScrollbar(guiGraphics);

        if (nameBox != null && nameBoxFocused)
            nameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        if (doneBtn != null)
            doneBtn.doRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    private int getScrollbarHeight() {
        int hH = CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();
        int bgH = CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        int fH = CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight();
        int bgCount = (windowHeight - hH - fH) / bgH;
        return hH + bgCount * bgH - 15;
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        ClientNetworkDataStorage.NetworkData networkData = ClientNetworkDataStorage.getNetworkData(networkId);
        if (networkData == null) return;

        int barX = guiLeft + windowWidth - 8;
        int barY = guiTop + 15;
        int barHeight = getScrollbarHeight();

        float scrollOffset = scroll.getValue();
        int visibleRows = getVisibleRows();
        int totalRows = getEffectivePlayerCount(networkData) + 2;
        int barSize = Math.max(10, (int) (barHeight * ((float) visibleRows / totalRows)));
        int scrollBarY = barY + (int) ((barHeight - barSize) * (scrollOffset / maxScroll));

        AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
        guiGraphics.blit(pad.location, barX, barY, pad.getWidth(), barHeight, pad.getStartX(), pad.getStartY(),
                pad.getWidth(), pad.getHeight(), 256, 256);

        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(guiGraphics, barX, scrollBarY);
        if (barSize > 16)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(guiGraphics, barX, scrollBarY + barSize / 2 - 4);
        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(guiGraphics, barX, scrollBarY + barSize - 5);
    }

    private List<UUID> getEffectivePlayers(ClientNetworkDataStorage.NetworkData networkData) {
        if (networkData.isOwnerMember && networkData.owner != null) {
            List<UUID> result = new ArrayList<>(networkData.players);
            if (!result.contains(networkData.owner)) {
                result.add(0, networkData.owner);
            }
            return result;
        }
        return networkData.players;
    }

    private int getEffectivePlayerCount(ClientNetworkDataStorage.NetworkData networkData) {
        return getEffectivePlayers(networkData).size();
    }

    public String getPlayerName(UUID uuid) {
        if (uuid == null) return "";
        if (minecraft == null || minecraft.level == null) return "";
        Player player = minecraft.level.getPlayerByUUID(uuid);
        if (player == null) return "";
        return player.getName().getString();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Do nothing - don't render the blur background
    }
}
