package de.theidler.create_mobile_packages;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RoboBee extends SimpleRobo{

    private ItemStack itemStack;

    public RoboBee(UUID id, Level level, Vec3 position, Vec3 deltaMovement, ItemStack itemStack) {
        super(id, level, position, deltaMovement);
        setItemStack(itemStack);
    }

    public RoboBee(Level level, Vec3 position, Vec3 deltaMovement, ItemStack itemStack) {
        this(UUID.randomUUID(), level, position, deltaMovement, itemStack);
    }

    @Override
    public void tick() {
        super.tick();
    }

    //region Getters/Setters
    public ItemStack getItemStack() {
        return itemStack;
    }
    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = ItemStack.EMPTY;
        }
        this.itemStack = itemStack;
    }
    //endregion

    @Override
    public void remove() {
        if (!itemStack.isEmpty()) {
            handleItemStackOnRemove(itemStack);
        }
        super.remove();
    }

    private void handleItemStackOnRemove(ItemStack itemStack) {
        if (PackageItem.isPackage(itemStack)){
            CMPHelper.sendPlayerChatMessage(CMPHelper.getPlayerFromPackageItemStack(itemStack, level), "RoboBee removed: " + itemStack.getHoverName().getString());
            level.addFreshEntity(PackageEntity.fromItemStack(level, this.getPosition(), itemStack));
        }
    }

    @Override
    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag nbt = super.write(dimensions);
        nbt.putInt("Type", RoboManager.RoboType.ROBO_BEE.ordinal());
        nbt.put("ItemStack", itemStack.save(new CompoundTag()));
        return nbt;
    }

    public static RoboBee read(CompoundTag tag, DimensionPalette dimensions) {
        UUID id = tag.getUUID("Id");
        Level level = CreateMobilePackages.ROBO_MANAGER.getLevel();

        Vec3 position = CMPHelper.readVec3FromTag(tag, "Pos");
        Vec3 deltaMovement = CMPHelper.readVec3FromTag(tag, "Delta");

        ItemStack itemstack = ItemStack.of(tag.getCompound("ItemStack"));

        return new RoboBee(id, level, position, deltaMovement, itemstack);
    }
}
