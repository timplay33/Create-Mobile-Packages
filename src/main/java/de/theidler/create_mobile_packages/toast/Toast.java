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

    public int getHeightWithSpacing() {
        return height + 4;
    }

    /**
     * Draws the toast.
     * @param guiGraphics the graphics object to draw on
     * @return height of the toast
     */
    public int draw(GuiGraphics guiGraphics, int x, int y, int toastWidth) {
        int h = height;

        // Outer dark border
        guiGraphics.fill(x, y, x + toastWidth, y + h,0xB3111111);
        // Main background
        guiGraphics.fill(x + 1, y + 1, x + toastWidth - 1, y + h - 1, 0xB31D1D1D);
        // Top highlight edge
        guiGraphics.fill(x + 1,y + 1, x + toastWidth - 1, y + 2,0xB3444444);
        // Inner inset shadow (bottom + right)
        guiGraphics.fill(x + 1, y + h - 2, x + toastWidth - 1, y + h - 1, 0xB3111111);
        guiGraphics.fill(x + toastWidth - 2, y + 1, x + toastWidth - 1, y + h - 1, 0xB3111111);
        // Left accent bar
        guiGraphics.fill(x + 1, y + 1, x + 3, y + h - 1,0xB3C8930A);
        // Bottom accent bar
        guiGraphics.fill(x + 3, y + h - 2, x + toastWidth - 1, y + h - 1, 0xB3C8930A);


        return getHeightWithSpacing();
    }
}
