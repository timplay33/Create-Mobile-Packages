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

    public CustomToast(UUID uuid, Component title, Component subtitle, ItemStack icon) {
        this.uuid = uuid;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomToast> STREAM_CODEC = StreamCodec.of(
        (buf, toast) -> {
            buf.writeUUID(toast.uuid);
            buf.writeUtf(toast.title.getString());
            buf.writeUtf(toast.subtitle.getString());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, toast.icon);
        },
        buf -> {
            UUID uuid = buf.readUUID();
            Component title = Component.literal(buf.readUtf());
            Component subtitle = Component.literal(buf.readUtf());
            ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new CustomToast(uuid, title, subtitle, icon);
        }
    );
}
