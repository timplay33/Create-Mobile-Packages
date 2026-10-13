package de.theidler.create_mobile_packages.toast.types;

import de.theidler.create_mobile_packages.index.CMPToasts;
import de.theidler.create_mobile_packages.toast.Toast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
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
    protected void writeData(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.STREAM_CODEC.encode(buf, title);
        ComponentSerialization.STREAM_CODEC.encode(buf, subtitle);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, icon);
        buf.writeInt(timeout);
    }

    public static SimpleToast readFromBuffer(RegistryFriendlyByteBuf buf, UUID uuid) {
        Component title = ComponentSerialization.STREAM_CODEC.decode(buf);
        Component subtitle = ComponentSerialization.STREAM_CODEC.decode(buf);
        ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int timeout = buf.readInt();
        return new SimpleToast(uuid, title, subtitle, icon, timeout);
    }

    @Override
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        Minecraft mc = Minecraft.getInstance();
        int heightWithSpacing = super.draw(guiGraphics, x, y, toastWidth);

        // Draw icon
        guiGraphics.renderItem(this.icon, x + 5, y + 6);
        // Draw title
        guiGraphics.drawString(mc.font, this.title, x + 22, y + 6, 0xFFFFFF, false);
        // Draw subtitle
        guiGraphics.drawString(mc.font, this.subtitle, x + 22, y + 18, 0xAAAAAA, false);
        return heightWithSpacing;
    }
}

