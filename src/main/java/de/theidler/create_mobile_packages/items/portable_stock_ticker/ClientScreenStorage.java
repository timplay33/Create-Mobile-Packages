package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import de.theidler.create_mobile_packages.index.CMPPackets;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.ArrayList;
import java.util.List;

public class ClientScreenStorage {
    public static List<GenericStack> stacks = new ArrayList<>();

    private static int ticks = 0;

    public static void tick() {
        if (ticks++ > 120) {
            update();
            ticks = 0;
        }
    }

    private static void update() {
        CMPPackets.getChannel().sendToServer(new RequestStockUpdate());
    }

    public static void manualUpdate() {
        update();
    }
}
