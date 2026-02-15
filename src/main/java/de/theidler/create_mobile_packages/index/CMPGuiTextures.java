package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public enum CMPGuiTextures implements ScreenElement, TextureSheetSegment {

    PLAYER_NETWORKS_HEADER("player_networks", 2, 7, 210, 16),
    PLAYER_NETWORKS_EDIT_NAME("player_networks", 230, 3, 13, 13),
    PLAYER_NETWORKS_BG("player_networks", 2, 36, 210, 22),
    PLAYER_NETWORKS_FOOTER("player_networks", 2, 73, 218, 31),
    PLAYER_NETWORKS_SLOT("player_networks", 2, 113, 210, 18)

    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    CMPGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    CMPGuiTextures(String location, int startX, int startY, int width, int height) {
        this(CreateMobilePackages.MODID, location, startX, startY, width, height);
    }

    CMPGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(@NotNull GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);

    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

    @Override
    public @NotNull ResourceLocation getLocation() {
        return location;
    }
}
