package de.theidler.create_mobile_packages.toast;

import net.minecraft.network.chat.Component;
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
}
