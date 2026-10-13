package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class CMPIcons implements ScreenElement {
    // from com.simibubi.create.foundation.gui.AllIcons
    public static final ResourceLocation ICON_ATLAS = ResourceLocation.fromNamespaceAndPath(CreateMobilePackages.MODID, "textures/gui/icons.png");
    public static final int ICON_ATLAS_SIZE = 256;

    private static int x = 0, y = -1;
    public static final CMPIcons
            I_RETURN = newRow(),
            I_DIRECT = next(),
            I_ROBO_BEE_AND_PACKAGE = next(),
            I_PACKAGE = next(),
            I_ROBO_BEE = next();

    private final int iconX;
    private final int iconY;


    public CMPIcons(int x, int y) {
        iconX = x * 16;
        iconY = y * 16;
    }

    private static CMPIcons next() {
        return new CMPIcons(++x, y);
    }

    private static CMPIcons newRow() {
        return new CMPIcons(x = 0, ++y);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(@NotNull GuiGraphics graphics, int x, int y) {
        graphics.blit(ICON_ATLAS, x, y, 0, iconX, iconY, 16, 16, ICON_ATLAS_SIZE, ICON_ATLAS_SIZE);
    }
}
