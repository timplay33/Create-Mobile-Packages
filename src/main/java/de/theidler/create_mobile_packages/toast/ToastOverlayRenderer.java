package de.theidler.create_mobile_packages.toast;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(
        value = Dist.CLIENT,
        modid = CreateMobilePackages.MODID
)
public class ToastOverlayRenderer {

    private static final List<CustomToast> TOASTS = new ArrayList<>();

    public static void showToast(CustomToast toast) {
        // replace Toast with same UUID or add new Toast
        for (int i = 0; i < TOASTS.size(); i++) {
            if (TOASTS.get(i).uuid.equals(toast.uuid)) {
                TOASTS.set(i, toast);
                return;
            }
        }
        TOASTS.add(toast);
    }

    public static void removeToast(UUID uuid) {
        TOASTS.removeIf(t -> t.uuid.equals(uuid));
    }

    public static void removeAllToasts() {
        TOASTS.clear();
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();

        int toastWidth = 160;
        int x = mc.getWindow().getGuiScaledWidth() - toastWidth - 10;
        int y = 10;

        TOASTS.removeIf(toast -> toast.lastUpdate < System.currentTimeMillis() - toast.timeout); // Remove toasts older than timeout

        for (CustomToast toast : TOASTS) {
            // Draw background rectangle
            guiGraphics.fill(x, y, x + toastWidth, y + 32, 0xAA000000);

            // Draw icon
            guiGraphics.renderItem(toast.icon, x + 6, y + 6);

            // Draw title
            guiGraphics.drawString(mc.font, toast.title, x + 28, y + 6, 0xFFFFFF, false);
            // Draw subtitle
            guiGraphics.drawString(mc.font, toast.subtitle, x + 28, y + 18, 0xAAAAAA, false);
            y += 36;
        }
    }
}
