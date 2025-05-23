package de.theidler.create_mobile_packages.items.robo_bee;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.states.FlyToTargetState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.function.Supplier;

public class RoboBeeItem extends ForgeSpawnEggItem {
    public RoboBeeItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Properties props) {
        super(type, backgroundColor, highlightColor, props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        ItemStack offhandItem = player.getOffhandItem();

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        EntityType<?> entityType = getType(null);

        Entity entity = entityType.create(level);
        if (entity == null) return InteractionResult.FAIL;

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0F, 0.0F);

        if (entity instanceof RoboBeeEntity roboBee) {
            if (offhandItem.getItem() instanceof PackageItem) {
                roboBee.setPackageHeightScale(1.0F);
                roboBee.setState(new FlyToTargetState());
                roboBee.setItemStack(offhandItem.copy());
                String address = PackageItem.getAddress(offhandItem);
                roboBee.setTargetAddress(address);
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY); // remove from offhand
            }
        }

        level.addFreshEntity(entity);
        context.getItemInHand().shrink(1);

        return InteractionResult.SUCCESS;
    }
}
