package de.theidler.create_mobile_packages.toast;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.index.config.CMPClient;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
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
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        CMPClient clientConfig = CMPConfigs.client();

        int toastWidth = 160;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        double guiScale = mc.getWindow().getGuiScale();
        CMPClient.ToastCorner corner = clientConfig.getToastCorner();
        int offsetX = (int) Math.round(clientConfig.toastOffsetX.get() / guiScale);
        int offsetY = (int) Math.round(clientConfig.toastOffsetY.get() / guiScale);

        boolean alignRight = corner == CMPClient.ToastCorner.TOP_RIGHT || corner == CMPClient.ToastCorner.BOTTOM_RIGHT;
        boolean alignBottom = corner == CMPClient.ToastCorner.BOTTOM_RIGHT || corner == CMPClient.ToastCorner.BOTTOM_LEFT;
        int x = alignRight ? screenWidth - toastWidth - offsetX : offsetX;

        TOASTS.removeIf(toast -> toast.lastUpdate < System.currentTimeMillis() - toast.timeout); // Remove toasts older than timeout

        if (alignBottom) {
            int y = screenHeight - offsetY + 4;
            for (Toast toast : TOASTS) {
                y -= toast.getHeightWithSpacing();
                toast.draw(guiGraphics, x, y, toastWidth);
            }
            return;
        }

        int y = offsetY;
        for (Toast toast : TOASTS) {
            y += toast.draw(guiGraphics, x, y, toastWidth);
        }
    }
}
