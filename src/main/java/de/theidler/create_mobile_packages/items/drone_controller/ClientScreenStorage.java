package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import net.createmod.catnip.platform.CatnipServices;

import java.util.ArrayList;
import java.util.List;

public class ClientScreenStorage {
    public static List<BigItemStack> stacks = new ArrayList<>();

    private static int ticks = 0;

    public static void tick() {
        if (ticks++ > 120) {
            update();
            ticks = 0;
        }
    }

    private static void update() {
        CatnipServices.NETWORK.sendToServer(new RequestStockUpdate());
    }

    public static void manualUpdate() {
        update();
    }
}
