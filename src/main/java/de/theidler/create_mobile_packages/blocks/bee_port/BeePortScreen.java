package de.theidler.create_mobile_packages.blocks.bee_port;

import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BeePortScreen extends PackagePortScreen {
    public BeePortScreen(PackagePortMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }


}
