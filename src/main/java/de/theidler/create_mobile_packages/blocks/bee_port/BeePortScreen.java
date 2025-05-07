package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BeePortScreen extends PackagePortScreen {
    public BeePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(graphics, pPartialTick, pMouseX, pMouseY);
        graphics.blit(new ResourceLocation(CreateMobilePackages.MODID,"textures/gui/bee_port.png"), getGuiLeft(), getGuiTop(), 0, 47, 220, 82);
    }
}
