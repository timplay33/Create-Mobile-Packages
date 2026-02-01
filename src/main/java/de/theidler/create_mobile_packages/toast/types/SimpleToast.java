package de.theidler.create_mobile_packages.toast.types;

import de.theidler.create_mobile_packages.index.CMPToasts;
import de.theidler.create_mobile_packages.toast.Toast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class SimpleToast extends Toast {
    public final Component title;
    public final Component subtitle;
    public final ItemStack icon;

    public SimpleToast(UUID uuid, Component title, Component subtitle, ItemStack icon, int timeout) {
        super(uuid, timeout);
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    public SimpleToast(UUID uuid, Component title, Component subtitle, ItemStack icon) {
        this(uuid, title, subtitle, icon, 5000);
    }

    @Override
    protected String getTypeId() {
        return CMPToasts.SIMPLE.id;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeComponent(title);
        buf.writeComponent(subtitle);
        buf.writeItem(icon);
        buf.writeInt(timeout);
    }

    public static SimpleToast readFromBuffer(FriendlyByteBuf buf, UUID uuid) {
        Component title = buf.readComponent();
        Component subtitle = buf.readComponent();
        ItemStack icon = buf.readItem();
        int timeout = buf.readInt();
        return new SimpleToast(uuid, title, subtitle, icon, timeout);
    }

    @Override
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        Minecraft mc = Minecraft.getInstance();
        int heightWithSpacing = super.draw(guiGraphics, x, y, toastWidth);

        // Draw icon
        guiGraphics.renderItem(this.icon, x + 6, y + 6);
        // Draw title
        guiGraphics.drawString(mc.font, this.title, x + 28, y + 6, 0xFFFFFF, false);
        // Draw subtitle
        guiGraphics.drawString(mc.font, this.subtitle, x + 28, y + 18, 0xAAAAAA, false);

        return heightWithSpacing;
    }
}

