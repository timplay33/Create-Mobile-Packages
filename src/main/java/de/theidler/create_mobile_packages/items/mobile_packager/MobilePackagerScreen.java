package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MobilePackagerScreen extends AbstractSimiContainerScreen<MobilePackagerMenu>
        implements ScreenWithStencils {

    private static final int CARD_HEADER = 20;
    private static final int CARD_WIDTH = 160;

    final int slices = 4;

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
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {

    }
}
