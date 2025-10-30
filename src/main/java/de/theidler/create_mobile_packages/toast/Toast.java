package de.theidler.create_mobile_packages.toast;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public abstract class Toast {

    private static final Map<String, BiFunction<RegistryFriendlyByteBuf, UUID, Toast>> REGISTRY = new HashMap<>();

    public static final StreamCodec<RegistryFriendlyByteBuf, Toast> STREAM_CODEC = StreamCodec.of(
        Toast::write,
        Toast::read
    );

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

    public static void register(String typeId, BiFunction<RegistryFriendlyByteBuf, UUID, Toast> factory) {
        REGISTRY.put(typeId, factory);
    }

    private static Toast read(RegistryFriendlyByteBuf buf) {
        String typeId = buf.readUtf(256);
        UUID uuid = buf.readUUID();
        BiFunction<RegistryFriendlyByteBuf, UUID, Toast> factory = REGISTRY.get(typeId);
        if (factory == null) {
            throw new IllegalStateException("Unknown toast type: " + typeId);
        }
        return factory.apply(buf, uuid);
    }

    private static void write(RegistryFriendlyByteBuf buf, Toast toast) {
        buf.writeUtf(toast.getTypeId());
        buf.writeUUID(toast.getId());
        toast.writeData(buf);
    }

    protected abstract String getTypeId();
    protected abstract void writeData(RegistryFriendlyByteBuf buf);

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
