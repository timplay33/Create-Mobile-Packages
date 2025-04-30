package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class MobilePackagerScreen extends AbstractSimiContainerScreen<MobilePackagerMenu>
        implements ScreenWithStencils {

    final int slices = 4;

    private EditBox editorEditBox;

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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int y = topPos - 5;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, leftPos, y);
        y += AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, leftPos, y);

        renderPlayerInventory(graphics, leftPos + 10, topPos + 88);

        FormattedCharSequence formattedcharsequence = Component.translatable("item.create_mobile_packages.mobile_packager")
                .getVisualOrderText();
        graphics.drawString(font, formattedcharsequence, AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getWidth() - font.width(formattedcharsequence)/4, topPos - 1, 0x3D3C48, false);
    }
}
