package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPItems;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MobilePackagerScreen extends AbstractSimiContainerScreen<MobilePackagerMenu>
        implements ScreenWithStencils {

    private String address = "";

    private EditBox addressBox;
    private IconButton confirmButton;

    public MobilePackagerScreen(MobilePackagerMenu container, Inventory inv, Component title) {
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

        if (addressBox == null) {
            addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 55, y + 68, 110, 10, false);
            addressBox.setValue(menu.getAddress());
            addressBox.setTextColor(0x555555);
        }
        addRenderableWidget(addressBox);

        confirmButton = new IconButton(x + bgWidth - 30, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);
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
/*
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY)) {
            menu.confirm(editorEditBox.getValue());
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            return true;
        }

        boolean wasNotFocused = editorEditBox != null && !editorEditBox.isFocused();
        boolean mouseClicked = super.mouseClicked(pMouseX, pMouseY, pButton);

        if (editorEditBox != null && editorEditBox.isMouseOver(pMouseX, pMouseY) && wasNotFocused) {
            editorEditBox.moveCursorToEnd();
            editorEditBox.setHighlightPos(0);
        }

        return mouseClicked;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (editorEditBox != null) {
            editorEditBox.tick();
            String currentAddress = menu.getAddress();
            if (!Objects.equals(currentAddress, address)) {
                address = currentAddress;
                editorEditBox.setValue(currentAddress);
                menu.packageInventory = menu.getContents();
            }
        }
        if (!(menu.player.getMainHandItem().getItem() instanceof MobilePackager)) {
            menu.player.closeContainer();
        }
    }*/
}
