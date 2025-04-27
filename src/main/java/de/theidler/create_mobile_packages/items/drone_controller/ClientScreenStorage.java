package de.theidler.create_mobile_packages.items.drone_controller;

import com.simibubi.create.content.logistics.BigItemStack;
import net.createmod.catnip.platform.CatnipServices;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientScreenStorage {
    public static List<BigItemStack> stacks = new ArrayList<>();

    private static int ticks = 0;

    public static void tick(UUID networkId) {
        if (ticks++ > 120) {
            update(networkId);
            ticks = 0;
        }
    }

    private static void update(UUID networkId) {
        CatnipServices.NETWORK.sendToServer(new RequestStockUpdate(networkId));
    }

    public static void manualUpdate(UUID networkId) {
        update(networkId);
    }
}
