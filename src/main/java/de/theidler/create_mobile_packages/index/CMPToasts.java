package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.toast.types.PackageToast;
import de.theidler.create_mobile_packages.toast.types.SimpleToast;
import de.theidler.create_mobile_packages.toast.Toast;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;
import java.util.function.BiFunction;

public enum CMPToasts {
    SIMPLE("simple", SimpleToast::readFromBuffer),
    PACKAGE("package", PackageToast::readFromBuffer);

    public final String id;
    private final BiFunction<FriendlyByteBuf, UUID, Toast> factory;

    CMPToasts(String id, BiFunction<FriendlyByteBuf, UUID, Toast> factory) {
        this.id = id;
        this.factory = factory;
    }

    public static void registerAll() {
        for (CMPToasts t : values()) {
            Toast.register(t.id, t.factory);
        }
    }
}
