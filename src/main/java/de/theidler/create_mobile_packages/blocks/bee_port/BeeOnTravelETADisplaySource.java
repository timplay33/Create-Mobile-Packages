package de.theidler.create_mobile_packages.blocks.bee_port;


import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BeeOnTravelETADisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof BeePortBlockEntity bpbe))
            return Component.empty();

        int eta = bpbe.getData().get(0);
        boolean isTraveling = bpbe.getData().get(1) == 1;
        if (!isTraveling)
            return Component.empty();
        return Component.literal(String.valueOf(eta));
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 20;
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
