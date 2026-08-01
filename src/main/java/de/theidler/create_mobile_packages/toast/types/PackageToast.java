package de.theidler.create_mobile_packages.toast.types;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import de.theidler.create_mobile_packages.compat.fluidlogistics.CFLClientBridge;
import de.theidler.create_mobile_packages.index.CMPToasts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageToast extends SimpleToast {

    public final List<ItemStack> items;

    public PackageToast(UUID uuid, Component title, Component subtitle, ItemStack icon, List<ItemStack> items, int timeout) {
        super(uuid, title, subtitle, icon, timeout);
        this.items = items;
        height = 50;
    }

    public PackageToast(UUID uuid, Component title, Component subtitle, ItemStack icon, List<ItemStack> items) {
        this(uuid, title, subtitle, icon, items, 5000);
    }

    @Override
    protected String getTypeId() {
        return CMPToasts.PACKAGE.id;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        super.writeData(buf);
        buf.writeVarInt(items.size());
        for (ItemStack item : items) {
            buf.writeItem(item);
        }
    }

    public static PackageToast readFromBuffer(FriendlyByteBuf buf, UUID uuid) {
        Component title = buf.readComponent();
        Component subtitle = buf.readComponent();
        ItemStack icon = buf.readItem();
        int timeout = buf.readInt();
        int size = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        return new PackageToast(uuid, title, subtitle, icon, items, timeout);
    }

    @Override
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        int heightWithSpacing = super.draw(guiGraphics, x, y, toastWidth);
        int spacing = toastWidth / items.size();
        // Draw Items
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            guiGraphics.renderItem(item, x + (i*spacing)+4, y + 30);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            if (!CFLClientBridge.tryRenderPackageResourceAmount(
                    guiGraphics, item, x + (i * spacing) + 4, y + 30)) {
                drawItemCount(guiGraphics, item.getCount(), x + (i * spacing) + 4, y + 30);
            }
            guiGraphics.pose().popPose();
        }

        return heightWithSpacing;
    }

    private void drawItemCount(GuiGraphics graphics, int count, int itemX, int itemY) {
        if (count < 1)
            return;

        String text = String.valueOf(count);
        int x = (int) Math.floor(-text.length() * 2.5);

        for (char c : text.toCharArray()) {
            int xOffset = (c - '0') * 6;

            RenderSystem.enableBlend();
            graphics.blit(NUMBERS.location, itemX + 14 + x, itemY + 10, 0,
                    NUMBERS.getStartX() + xOffset, NUMBERS.getStartY(),
                    NUMBERS.getWidth(), NUMBERS.getHeight(), 256, 256);
            x += NUMBERS.getWidth() - 1;
        }
    }

    private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
}
