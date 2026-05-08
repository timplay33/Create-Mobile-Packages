package de.theidler.create_mobile_packages.toast.types;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import de.theidler.create_mobile_packages.compat.Mods;
import de.theidler.create_mobile_packages.compat.fluidlogistics.CFLBridge;
import de.theidler.create_mobile_packages.compat.fluidlogistics.CFLClientBridge;
import de.theidler.create_mobile_packages.index.CMPToasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageToast extends SimpleToast {

    public final List<ItemStack> items;

    public PackageToast(UUID uuid, Component title, Component subtitle, ItemStack icon, List<ItemStack> items, int timeout) {
        super(uuid, title, subtitle, icon, timeout);
        this.items = items.stream()
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        height = items.isEmpty() ? 32 : 52;
    }

    public PackageToast(UUID uuid, Component title, Component subtitle, ItemStack icon, List<ItemStack> items) {
        this(uuid, title, subtitle, icon, items, 5000);
    }

    @Override
    protected String getTypeId() {
        return CMPToasts.PACKAGE.id;
    }

    @Override
    protected void writeData(RegistryFriendlyByteBuf buf) {
        super.writeData(buf);
        buf.writeVarInt(items.size());
        for (ItemStack item : items) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
        }
    }

    public static PackageToast readFromBuffer(RegistryFriendlyByteBuf buf, UUID uuid) {
        Component title = ComponentSerialization.STREAM_CODEC.decode(buf);
        Component subtitle = ComponentSerialization.STREAM_CODEC.decode(buf);
        ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int timeout = buf.readInt();
        int size = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
        return new PackageToast(uuid, title, subtitle, icon, items, timeout);
    }

    @Override
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        int heightWithSpacing = super.draw(guiGraphics, x, y, toastWidth);

        guiGraphics.fill(x + 4, y + 28, x + toastWidth - 4, y + 29, 0xFF2F2F2F);

        int slotSize = 16;
        int slotGap = 3;
        int startX = x + 5;
        int slotY = y + 31;
        boolean hasOverflow = items.size() > 8;
        int maxVisible = hasOverflow ? 7 : 8;
        int visibleItems = Math.min(items.size(), maxVisible);

        // Draw Items
        for (int i = 0; i < 8; i++) {
            int sx = startX + i * (slotSize + slotGap);

            guiGraphics.fill(sx - 1, slotY - 1, sx + slotSize, slotY + slotSize, 0xFF2A2A2A);
            guiGraphics.fill(sx - 1, slotY - 1, sx + slotSize, slotY, 0xFF3A3A3A);

            if (i < visibleItems) {
                ItemStack item = items.get(i);
                guiGraphics.renderItem(item, sx, slotY);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                if (Mods.FLUIDLOGISTICS.isLoaded()
                        && CFLBridge.shouldDisplayAsFluidInPackage(item)) {
                    CFLClientBridge.renderPackageFluidAmount(guiGraphics, item, sx, slotY);
                } else {
                    drawItemCount(guiGraphics, item.getCount(), sx, slotY);
                }
                guiGraphics.pose().popPose();
            }
        }

        if (hasOverflow) {
            int overflow = items.size() - maxVisible;
            int sx = startX + maxVisible * (slotSize + slotGap);

            guiGraphics.fill(sx + 3, slotY + 1, sx + slotSize + 3, slotY + slotSize + 1, 0xFF1A1A1A);
            guiGraphics.fill(sx + 1, slotY + 1, sx + slotSize + 1, slotY + slotSize + 1, 0xFF222200);
            guiGraphics.fill(sx - 1, slotY - 1, sx + slotSize, slotY + slotSize, 0xFF3D2E00);
            guiGraphics.fill(sx - 1, slotY - 1, sx + slotSize, slotY, 0xFF5A4500);
            guiGraphics.fill(sx - 1, slotY - 1, sx + 1, slotY + slotSize, 0xFFC8930A);

            String overflowText = "+" + overflow;
            int textWidth = Minecraft.getInstance().font.width(overflowText);
            guiGraphics.drawString(Minecraft.getInstance().font, overflowText,
                    sx + (slotSize / 2) - (textWidth / 2),
                    slotY + (slotSize / 2) - 4,
                    0xF5C842, false);
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
