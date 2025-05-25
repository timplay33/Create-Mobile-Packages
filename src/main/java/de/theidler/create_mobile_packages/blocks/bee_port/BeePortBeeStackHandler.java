package de.theidler.create_mobile_packages.blocks.bee_port;

import de.theidler.create_mobile_packages.index.CMPItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BeePortBeeStackHandler extends SlotItemHandler {

    public BeePortBeeStackHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.getItem() == CMPItems.ROBO_BEE.get()) {
            return super.mayPlace(stack);
        }
        return false;
    }
}
