package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPItems;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MobilePackagerScreen extends AbstractSimiContainerScreen<MobilePackagerMenu> {

    private IconButton confirmButton;

    public MobilePackagerScreen(MobilePackagerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getHeight();
        int bgWidth = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();
        int x = getGuiLeft();
        int y = getGuiTop();

        confirmButton = new IconButton(x + bgWidth - 40, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> openEditScreen());
        addRenderableWidget(confirmButton);
    }

    private void openEditScreen() {
        menu.firstConfirmClicked = true;
        if (!menu.packageSlotInventory.getStackInSlot(0).isEmpty()) {
            for (int i = 0; i < menu.getContents().getSlots(); i++) {
                menu.packageContentsInventory.setStackInSlot(i, menu.getContents().getStackInSlot(i).copy());
            }
            Minecraft.getInstance().setScreen(new MobilePackagerEditScreen(menu, menu.player.getInventory(), Component.translatable("item.create_mobile_packages.mobile_packager")));
        } else {
            Minecraft.getInstance().player.closeContainer();
        }
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = getGuiLeft();
        int y = getGuiTop();
        AllGuiTextures.FACTORY_GAUGE_SET_ITEM.render(pGuiGraphics, x - 5, y);
        renderPlayerInventory(pGuiGraphics, x + 5, y + 94);

        ItemStack stack = CMPItems.MOBILE_PACKAGER.asStack();
        Component title = CreateLang.translate("gui.factory_panel.place_item_to_monitor")
                .component();
        pGuiGraphics.drawString(font, title, x + imageWidth / 2 - font.width(title) / 2 - 5, y + 4, 0x3D3C48, false);

        GuiGameElement.of(stack)
                .scale(3)
                .render(pGuiGraphics, x + 180, y + 48);
    }

}

class MobilePackagerEditScreen extends AbstractSimiContainerScreen<MobilePackagerMenu> {

    private EditBox addressBox;
    private IconButton confirmButton;

    public MobilePackagerEditScreen(MobilePackagerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
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
        menu.hasEditMenu = true;
        menu.addSlots();

        if (addressBox == null) {
            addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 55, y + 68, 110, 10, false);
            addressBox.setValue(menu.getAddress());
            addressBox.setTextColor(0x555555);
        }
        addRenderableWidget(addressBox);

        confirmButton = new IconButton(x + bgWidth - 30, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> closeContainer());
        addRenderableWidget(confirmButton);
    }

    private void closeContainer() {
        menu.writeContents();
        minecraft.player.closeContainer();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
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
