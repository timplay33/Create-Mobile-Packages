package de.theidler.create_mobile_packages.index;

import com.mojang.blaze3d.platform.InputConstants;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public enum CMPKeys {
    // From Create AllKeys.java
    OPEN_PORTABLE_STOCK_TICKER("open_portable_stock_ticker", GLFW.GLFW_KEY_G);

    private KeyMapping keybind;
    private final String description;
    private final int key;
    private final boolean modifiable;

    CMPKeys(String description, int defaultKey) {
        this.description = CreateMobilePackages.MODID + ".keyinfo." + description;
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for (CMPKeys key : values()) {
            key.keybind = new KeyMapping(key.description, key.key, CreateMobilePackages.NAME);
            if (!key.modifiable)
                continue;

            event.register(key.keybind);
        }
    }

    public boolean isPressed() {
        if (!modifiable)
            return isKeyDown(key);
        return keybind.isDown();
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance()
                .getWindow()
                .getWindow(), key);
    }
}
