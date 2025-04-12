package de.theidler.create_mobile_packages.blocks;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DronePortScreen extends AbstractSimiContainerScreen<DronePortMenu> implements ScreenWithStencils {

    public DronePortScreen(DronePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        renderPlayerInventory(pGuiGraphics, leftPos + 10, topPos + 88);
    }

    public void renderPlayerInventory(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.PLAYER_INVENTORY.render(graphics, x, y);
        graphics.drawString(font, playerInventoryTitle, x + 8, y + 6, 0x404040, false);
    }
}
