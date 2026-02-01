package de.theidler.create_mobile_packages.network_settings;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.index.CMPGuiTextures;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.animation.LerpedFloat;
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
    private LogisticsNetwork network;
    private IExtendedLogisticsNetwork extendedNetwork;
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

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (width - windowWidth) / 2;
        this.guiTop = (height - windowHeight) / 2;

        network = Create.LOGISTICS.logisticsNetworks.get(networkId);
        extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(network);

        refreshUI();
    }

    private void refreshUI() {
        this.clearWidgets();
        this.playerButtons.clear();

        if (network == null || extendedNetwork == null) {
            minecraft.setScreen(parent);
            return;
        }

        createNameBox();
        createLockButton();
        createPlayerList();
        createAddPlayerButton();

        doneBtn = new IconButton(guiLeft + windowWidth - 25, guiTop + windowHeight - 43, AllIcons.I_CONFIRM);
        doneBtn.withCallback(() -> {
            minecraft.setScreen(parent);
        });
        addRenderableWidget(doneBtn);
    }


    private void createLockButton() {
        networkLockButton = new IconButton(guiLeft + windowWidth - 30, guiTop + 25, network.locked ? AllIcons.I_CONFIG_UNLOCKED : AllIcons.I_CONFIG_LOCKED);
        networkLockButton.setToolTip(Component.translatable(network.locked ? "create.gui.stock_keeper.network_locked" : "create.gui.stock_keeper.network_open"));
        networkLockButton.withCallback(() -> {
            CMPPackets.getChannel().sendToServer(new ModifyNetworkLockStatePackage(!network.locked, networkId));
            network.locked = !network.locked; // do on the client side for immediate feedback
            networkLockButton.setIcon(network.locked ? AllIcons.I_CONFIG_UNLOCKED : AllIcons.I_CONFIG_LOCKED);
            networkLockButton.setToolTip(Component.translatable(network.locked ? "create.gui.stock_keeper.network_locked" : "create.gui.stock_keeper.network_open"));
        });
        addRenderableWidget(networkLockButton);
    }

    private void createPlayerList() {
        List<UUID> players = extendedNetwork.create_mobile_packages$getPlayers().stream().toList();
        for (int i = 0; i < players.size(); i++) {
            UUID pId = players.get(i);

            IconButton removeBtn = new IconButton(0, 0, AllIcons.I_MTD_CLOSE);
            removeBtn.setToolTip(Component.translatable("tooltip.create_mobile_packages.network.remove_player"));
            removeBtn.withCallback(() -> {
                CMPPackets.getChannel().sendToServer(new RemovePlayerFromNetworkPackage(pId, networkId));
                extendedNetwork.create_mobile_packages$removePlayer(pId); // update UI local
                this.refreshUI(); // redraw UI
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
            CMPPackets.getChannel().sendToServer(new AddPlayerToNetworkPackage(player.getUUID(), networkId));
            extendedNetwork.create_mobile_packages$addPlayer(player.getUUID()); // update UI local
            this.refreshUI(); // redraw UI
        });
        addRenderableWidget(addPlayerButton);
    }

    private void createNameBox() {
        Consumer<String> onTextChanged = s -> {
            nameBox.setX(nameBoxX(s, nameBox));
            //save network name
            CMPPackets.getChannel().sendToServer(new SetNetworkNamePackage(nameBox.getValue(), networkId));
        };
        nameBox = new EditBox(new NoShadowFontWrapper(font), guiLeft + 25, guiTop + 4, windowWidth - 50, 10, Component.empty());
        nameBox.setMaxLength(25);
        nameBox.setBordered(false);
        if (extendedNetwork != null) {
            nameBox.setValue(extendedNetwork.create_mobile_packages$getName());
        }
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
        if (!nameBox.isFocused()) {
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
            int barX = guiLeft + windowWidth - 10;
            int barY = guiTop + 25;
            int barWidth = 6;
            int barHeight = 106;
            if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
                scrollHandleActive = true;
                return true;
            }
        }
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!result && nameBox.isFocused()) {
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
            int barHeight = 106;
            double relativeY = mouseY - (guiTop + 25);
            float target = (float) (relativeY / barHeight * maxScroll);
            scroll.chase(Mth.clamp(target, 0, maxScroll), 0.5f, LerpedFloat.Chaser.EXP);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private int getMaxScroll() {
        return Math.max(0, extendedNetwork.create_mobile_packages$getPlayers().size() - 4);
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
        int x = guiLeft;
        int y = guiTop;

        CMPGuiTextures.PLAYER_NETWORKS_HEADER.render(graphics, x, y);
        y += CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight();
        for (int i = 0; i < (windowHeight - CMPGuiTextures.PLAYER_NETWORKS_HEADER.getHeight() - CMPGuiTextures.PLAYER_NETWORKS_FOOTER.getHeight()) / CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight(); i++) {
            CMPGuiTextures.PLAYER_NETWORKS_BG.render(graphics, x, y);
            y += CMPGuiTextures.PLAYER_NETWORKS_BG.getHeight();
        }
        CMPGuiTextures.PLAYER_NETWORKS_FOOTER.render(graphics, x, y);

        String text = nameBox.getValue();
        nameBox.visible = nameBox.isFocused();
        if (!nameBox.isFocused()) {
            graphics.drawString(font, text, nameBoxX(text, nameBox), guiTop + 4, 0x4A2D31, false);
            CMPGuiTextures.PLAYER_NETWORKS_EDIT_NAME.render(graphics, nameBoxX(text, nameBox) + font.width(text) + 5, guiTop + 1);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        guiGraphics.drawString(font, Component.translatable("create_mobile_packages.network.owner", getPlayerName(network.owner)), guiLeft + 20, guiTop + 30, 0x3D3C48, false);

        guiGraphics.drawString(font, Component.translatable("create_mobile_packages.network.players"), guiLeft + 20, guiTop + 50, 0x3D3C48, false);

        float scrollOffset = scroll.getValue(partialTick);
        int listTop = guiTop + 60;
        int listBottom = guiTop + windowHeight - 10;

        guiGraphics.enableScissor(guiLeft, listTop, guiLeft + windowWidth, listBottom);

        List<UUID> players = extendedNetwork.create_mobile_packages$getPlayers().stream().toList();
        for (int i = 0; i < players.size(); i++) {
            float rowY = listTop + 5 + (i - scrollOffset) * 20;

            if (rowY + 15 < listTop || rowY > listBottom) {
                if (i < playerButtons.size()) {
                    playerButtons.get(i).visible = false;
                }
                continue;
            }

            guiGraphics.drawString(font, getPlayerName(players.get(i)), guiLeft + 30, (int) rowY, 0x555555, false);

            if (i < playerButtons.size()) {
                IconButton removeBtn = playerButtons.get(i);
                removeBtn.setX(guiLeft + windowWidth - 30);
                removeBtn.setY((int) rowY - 5);
                removeBtn.visible = true;
            }
        }

        guiGraphics.disableScissor();

        renderScrollbar(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        int barX = guiLeft + windowWidth - 10;
        int barY = guiTop + 25;
        int barHeight = 106;

        float scrollOffset = scroll.getValue();
        int barSize = Math.max(10, (int) (barHeight * (4f / (extendedNetwork.create_mobile_packages$getPlayers().size()))));
        int scrollBarY = barY + (int) ((barHeight - barSize) * (scrollOffset / maxScroll));

        AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
        guiGraphics.blit(pad.location, barX, barY, pad.getWidth(), barHeight, pad.getStartX(), pad.getStartY(),
                pad.getWidth(), pad.getHeight(), 256, 256);

        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(guiGraphics, barX, scrollBarY);
        if (barSize > 16)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(guiGraphics, barX, scrollBarY + barSize / 2 - 4);
        AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(guiGraphics, barX, scrollBarY + barSize - 5);
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
