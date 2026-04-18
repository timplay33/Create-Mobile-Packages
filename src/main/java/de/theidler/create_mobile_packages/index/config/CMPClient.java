package de.theidler.create_mobile_packages.index.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class CMPClient extends ConfigBase {

    public final ConfigInt toastCorner = i(1, 0, 3, "toastCorner",
            Comments.toastCorner);
    public final ConfigInt toastOffsetX = i(10, -10000, 10000, "toastOffsetX",
            Comments.toastOffsetX);
    public final ConfigInt toastOffsetY = i(10, -10000, 10000, "toastOffsetY",
            Comments.toastOffsetY);

    @Override
    public @NotNull String getName() {
        return "client";
    }

    public ToastCorner getToastCorner() {
        return ToastCorner.fromIndex(toastCorner.get());
    }

    public enum ToastCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT;

        public static ToastCorner fromIndex(int index) {
            return switch (index) {
                case 0 -> TOP_LEFT;
                case 1 -> TOP_RIGHT;
                case 2 -> BOTTOM_RIGHT;
                case 3 -> BOTTOM_LEFT;
                default -> TOP_RIGHT;
            };
        }
    }

    private static class Comments {
        static String toastCorner = "Which corner custom toasts are anchored to. 0 = top left, 1 = top right, 2 = bottom right, 3 = bottom left. Default: 1";
        static String toastOffsetX = "Horizontal offset for custom toasts in pixels, measured inward from the anchored edge. Positive values move inward, negative values move outward. Default: 10";
        static String toastOffsetY = "Vertical offset for custom toasts in pixels, measured inward from the anchored edge. Positive values move inward, negative values move outward. Default: 10";
    }
}
