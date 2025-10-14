package de.theidler.create_mobile_packages.toast;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CustomToast {
    public final UUID uuid;
    public final Component title;
    public final Component subtitle;
    public final ItemStack icon;
    public final long lastUpdate = System.currentTimeMillis();
    public final int timeout;

    public CustomToast(UUID uuid, Component title, Component subtitle, ItemStack icon, int timeout) {
        this.uuid = uuid;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
        this.timeout = timeout;
    }

    public CustomToast(UUID uuid, Component title, Component subtitle, ItemStack icon) {
        this(uuid, title, subtitle, icon, 5000);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomToast> STREAM_CODEC = StreamCodec.of(
        (buf, toast) -> {
            buf.writeUUID(toast.uuid);
            buf.writeUtf(toast.title.getString());
            buf.writeUtf(toast.subtitle.getString());
            buf.writeInt(toast.timeout);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, toast.icon);
        },
        buf -> {
            UUID uuid = buf.readUUID();
            Component title = Component.literal(buf.readUtf());
            Component subtitle = Component.literal(buf.readUtf());
            int timeout = buf.readInt();
            ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new CustomToast(uuid, title, subtitle, icon, timeout);
        }
    );
}
