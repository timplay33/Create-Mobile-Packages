package de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import de.theidler.create_mobile_packages.index.CMPGuiTextures;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class TrashScreen extends AbstractSimiContainerScreen<TrashMenu> {

    private AddressEditBox addressBox;
    private String lastSyncedAddress = "";

    public TrashScreen(TrashMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        int bgWidth = CMPGuiTextures.TRASH_MENU.getWidth();
        int bgHeight = CMPGuiTextures.TRASH_MENU.getHeight();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();
        int x = getGuiLeft();
        int y = getGuiTop();

        String previousAddress = addressBox == null ? menu.getTargetAddress() : addressBox.getValue();
        lastSyncedAddress = previousAddress;
        addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 38, y + 39, 160, 10,
                true);
        addressBox.setValue(previousAddress);
        addressBox.setTextColor(0x555555);
        addRenderableWidget(addressBox);
    }

    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
        // Check if address has changed and sync to server (client initiates, server handles)
        String currentAddress = addressBox.getValue();
        if (!currentAddress.equals(lastSyncedAddress)) {
            lastSyncedAddress = currentAddress;
            // Send to server - server-side will handle RoboManager update
            CatnipServices.NETWORK.sendToServer(new SyncTrashAddressPacket(currentAddress));
        }
    }

    @Override
    public void onClose() {
        // Save the address one final time when closing (send to server)
        String currentAddress = addressBox.getValue();
        if (!currentAddress.equals(lastSyncedAddress)) {
            CatnipServices.NETWORK.sendToServer(new SyncTrashAddressPacket(currentAddress));
        }
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        // Handle addressBox focus and clicks
        if (addressBox.isFocused()) {
            // When focused, let addressBox handle clicks in its area (including suggestions dropdown)
            if (addressBox.mouseClicked(pMouseX, pMouseY, pButton))
                return true;
            // Clicked outside - unfocus
            addressBox.setFocused(false);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (addressBox.isFocused() && addressBox.keyPressed(pKeyCode, pScanCode, pModifiers))
            return true;
        return addressBox.isFocused() && pKeyCode != 256 || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (addressBox.isFocused() && addressBox.charTyped(pCodePoint, pModifiers))
            return true;
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        // Prevent slot hover highlighting when addressBox suggestions dropdown is showing
        if (addressBox.isFocused())
            return false;
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        if (minecraft != null && this != minecraft.screen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        PoseStack ms = guiGraphics.pose();

        ms.pushPose();

        int x = getGuiLeft();
        int y = getGuiTop();

        CMPGuiTextures.TRASH_MENU.render(guiGraphics, x, y - 20);
        renderPlayerInventory(guiGraphics, x + 40, y + 66);

        MutableComponent headerTitle = Component.translatable(
                "item.create_mobile_packages.portable_stock_ticker.trash_menu");
        guiGraphics.drawString(font, headerTitle, x + 256 / 2 - font.width(headerTitle) / 2, y - 16, 0x714A40,
                false);

        ms.popPose();
    }

    public void applyTargetAddress(String address) {
        String resolved = address != null ? address : "";
        lastSyncedAddress = resolved;
        if (addressBox != null) {
            addressBox.setValue(resolved);
        }
        menu.setTargetAddress(resolved);
    }
}
