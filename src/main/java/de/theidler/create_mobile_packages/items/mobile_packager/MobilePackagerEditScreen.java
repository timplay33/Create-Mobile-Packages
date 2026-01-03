package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MobilePackagerEditScreen extends AbstractSimiContainerScreen<MobilePackagerEditMenu> {

    private AddressEditBox addressBox;
    private IconButton confirmButton;
    private final Inventory playerInventory;

    public MobilePackagerEditScreen(MobilePackagerEditMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.playerInventory = inv;
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
        int bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();
        int x = getGuiLeft();
        int y = getGuiTop();
        menu.addSlots();

        String previousAddress = addressBox == null ? menu.contentHolder.address : addressBox.getValue();
        addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 55, y + 68, 110,
                10, true, "@" + playerInventory.player.getName().getString());
        addressBox.setValue(previousAddress);
        addressBox.setTextColor(0x555555);
        addRenderableWidget(addressBox);

        confirmButton = new IconButton(x + bgWidth - 30, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            CMPPackets.getChannel().sendToServer(new ConfirmEditMenuPacket(addressBox.getValue()));
        });
        addRenderableWidget(confirmButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (addressBox.mouseScrolled(mouseX, mouseY, delta))
            return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
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
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = getGuiLeft();
        int y = getGuiTop();
        AllGuiTextures.REDSTONE_REQUESTER.render(pGuiGraphics, x + 3, y);
        renderPlayerInventory(pGuiGraphics, x + 25, y + 124);

        ItemStack stack = CMPItems.MOBILE_PACKAGER.asStack();
        Component title = CreateLang.text(stack.getHoverName()
                        .getString())
                .component();
        pGuiGraphics.drawString(font, title, x + 117 - font.width(title) / 2, y + 4, 0x3D3C48, false);

        GuiGameElement.of(stack)
                .scale(3)
                .render(pGuiGraphics, x + 245, y + 80);
    }
}