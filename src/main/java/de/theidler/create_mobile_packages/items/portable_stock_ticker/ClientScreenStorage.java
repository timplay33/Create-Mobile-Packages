package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import net.createmod.catnip.platform.CatnipServices;

import java.util.ArrayList;
import java.util.List;

public class ClientScreenStorage {
    public static final int TICKS_BETWEEN_UPDATES = 100;
    public static List<GenericStack> stacks = new ArrayList<>();
    private static List<GenericStack> collectionBuffer = new ArrayList<>();

    private static int ticks = 0;

    public static void tick() {
        if (ticks++ > TICKS_BETWEEN_UPDATES) {
            update();
            ticks = 0;
        }
    }

    private static void update() {
        CatnipServices.NETWORK.sendToServer(RequestStockUpdate.INSTANCE);
    }

    public static void manualUpdate() {
        update();
    }

    public static void receiveChunk(List<GenericStack> chunks, boolean last) {
        collectionBuffer.addAll(chunks);
        if (last) {
            stacks = collectionBuffer;
            collectionBuffer = new ArrayList<>();
        }
    }
}
