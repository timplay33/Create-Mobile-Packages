package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;

public class MobilePackagerScreen extends AbstractSimiContainerScreen<MobilePackagerMenu>
        implements ScreenWithStencils {

    final int slices = 6;
    private String address = "";

    private EditBox editorEditBox;
    private IconButton editorConfirm;

    public MobilePackagerScreen(MobilePackagerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        AllGuiTextures bg = AllGuiTextures.STOCK_KEEPER_CATEGORY;
        setWindowSize(bg.getWidth(), bg.getHeight() * slices + AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight()
                + AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight());
        super.init();
        clearWidgets();

        editorConfirm = new IconButton(leftPos + 36 + 131, topPos + 79, AllIcons.I_CONFIRM);

        editorEditBox = new EditBox(font, leftPos + 47, topPos + 28, 124, 10, Component.literal(menu.getAddress()));
        editorEditBox.setTextColor(0xffeeeeee);
        editorEditBox.setBordered(false);
        editorEditBox.setFocused(false);
        editorEditBox.mouseClicked(0, 0, 0);
        editorEditBox.setMaxLength(28);

        addRenderableWidget(editorConfirm);
        addRenderableWidget(editorEditBox);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int y = topPos - 5;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, leftPos, y);

        renderPlayerInventory(graphics, leftPos + 10, topPos + 88);

        FormattedCharSequence formattedcharsequence = Component.translatable("item.create_mobile_packages.mobile_packager")
                .getVisualOrderText();
        graphics.drawString(font, formattedcharsequence, leftPos + AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getWidth()/2 - font.width(formattedcharsequence)/2, topPos - 1, 0x3D3C48, false);
    }

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
    }
}
