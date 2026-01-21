package de.theidler.create_mobile_packages.toast;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ToastOverlayRenderer {

    private static final List<Toast> TOASTS = new ArrayList<>();

    public static void showToast(Toast toast) {
        // replace Toast with the same UUID or add new Toast
        for (int i = 0; i < TOASTS.size(); i++) {
            if (TOASTS.get(i).getId().equals(toast.getId())) {
                TOASTS.set(i, toast);
                return;
            }
        }
        TOASTS.add(toast);
    }

    public static void removeToast(UUID uuid) {
        TOASTS.removeIf(t -> t.getId().equals(uuid));
    }

    public static void removeAllToasts() {
        TOASTS.clear();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        int toastWidth = 160;
        int x = event.getWindow().getGuiScaledWidth() - toastWidth - 10;
        int y = 10;

        TOASTS.removeIf(toast -> toast.lastUpdate < System.currentTimeMillis() - toast.timeout); // Remove toasts older than timeout

        for (Toast toast : TOASTS) {
            y += toast.draw(guiGraphics, x, y, toastWidth);
        }
    }
}
