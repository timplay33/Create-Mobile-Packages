package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.index.CMPItems;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
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
        confirmButton.withCallback(menu::confirm);
        addRenderableWidget(confirmButton);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = getGuiLeft();
        int y = getGuiTop();
        AllGuiTextures.FACTORY_GAUGE_SET_ITEM.render(pGuiGraphics, x - 5, y);
        renderPlayerInventory(pGuiGraphics, x + 5, y + 94);

        ItemStack stack = CMPItems.MOBILE_PACKAGER.asStack();
        Component title = Component.translatable("item.create_mobile_packages.mobile_packager");
        pGuiGraphics.drawString(font, title, x + imageWidth / 2 - font.width(title) / 2 - 5, y + 4, 0x3D3C48, false);

        GuiGameElement.of(stack)
                .scale(3)
                .render(pGuiGraphics, x + 180, y + 48);
    }

}
