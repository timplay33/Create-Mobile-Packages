package de.theidler.create_mobile_packages.toast;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public abstract class Toast {

    private static final Map<String, BiFunction<FriendlyByteBuf, UUID, Toast>> REGISTRY = new HashMap<>();

    private final UUID id;
    public final long lastUpdate = System.currentTimeMillis();
    public final int timeout;
    protected int backgroundColor = 0xAA000000;
    protected int height = 32;

    protected Toast(UUID id, int timeout) {
        this.id = id;
        this.timeout = timeout;
    }

    public UUID getId() {
        return id;
    }

    public static void register(String typeId, BiFunction<FriendlyByteBuf, UUID, Toast> factory) {
        REGISTRY.put(typeId, factory);
    }

    public static Toast read(FriendlyByteBuf buf) {
        String typeId = buf.readUtf(256);
        UUID uuid = buf.readUUID();
        BiFunction<FriendlyByteBuf, UUID, Toast> factory = REGISTRY.get(typeId);
        if (factory == null) {
            throw new IllegalStateException("Unknown toast type: " + typeId);
        }
        return factory.apply(buf, uuid);
    }

    public final void write(FriendlyByteBuf buf) {
        buf.writeUtf(getTypeId());
        buf.writeUUID(getId());
        writeData(buf);
    }

    protected abstract String getTypeId();
    protected abstract void writeData(FriendlyByteBuf buf);

    /**
     * Draws the toast.
     * @param guiGraphics the graphics object to draw on
     * @return height of the toast
     */
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        // Draw background rectangle
        guiGraphics.fill(x, y, x + toastWidth, y + height, backgroundColor);
        return height + 4;
    }
}
