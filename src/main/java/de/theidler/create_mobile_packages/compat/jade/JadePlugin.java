package de.theidler.create_mobile_packages.compat.jade;

import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BeePortComponentProvider.INSTANCE, BeePortBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BeePortComponentProvider.INSTANCE, BeePortBlock.class);
    }
}
