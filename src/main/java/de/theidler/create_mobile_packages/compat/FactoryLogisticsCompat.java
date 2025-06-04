package de.theidler.create_mobile_packages.compat;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import java.util.UUID;

public class FactoryLogisticsCompat {
    public static boolean tryBroadcast(UUID freq, LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler, String address) {
        /*return ru.zznty.create_factory_logistics.logistics.panel.request.IngredientLogisticsManager.broadcastPackageRequest(
                freq, type,
                ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder.of(order),
                ignoredHandler, address
        );*/
        return true;
    }
}
