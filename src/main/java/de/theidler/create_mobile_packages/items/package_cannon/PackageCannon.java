package de.theidler.create_mobile_packages.items.package_cannon;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PackageCannon extends ProjectileWeaponItem implements CustomArmPoseItem {

    public static final int MAX_DAMAGE = 100;

    public PackageCannon(Properties properties) {
        super(properties.stacksTo(1).defaultDurability(MAX_DAMAGE));
    }

    public static Ammo getAmmo(Player player, ItemStack heldStack) {
        ItemStack ammoStack = player.getProjectile(heldStack);
        if (ammoStack.isEmpty() || !PackageItem.isPackage(ammoStack)) {
            return null;
        }

        return new Ammo(ammoStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldStack = player.getItemInHand(usedHand);
        if (ShootableGadgetItemMethods.shouldSwap(player, heldStack, usedHand, stack -> stack.getItem() instanceof PackageCannon)) {
            return InteractionResultHolder.fail(heldStack);
        }

        Ammo ammo = getAmmo(player, heldStack);
        if (ammo == null) {
            return InteractionResultHolder.pass(heldStack);
        }

        ItemStack ammoStack = ammo.stack();

        if (level.isClientSide) {
            return InteractionResultHolder.success(heldStack);
        }

        Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, usedHand == InteractionHand.MAIN_HAND, new Vec3(.75f, -.7f, 1.5f));
        Vec3 correction = ShootableGadgetItemMethods.getGunBarrelVec(player, usedHand == InteractionHand.MAIN_HAND, new Vec3(-.05f, 0, 0))
                .subtract(player.position().add(0, player.getEyeHeight(), 0));

        Vec3 lookVec = player.getLookAngle();
        Vec3 motion = lookVec.add(correction)
                .normalize()
                .scale(3);

        float soundPitch = (level.getRandom().nextFloat() - .5f) / 4f;

        PackageEntity pEnt = PackageEntity.fromItemStack(level, barrelPos, ammoStack);
        pEnt.addDeltaMovement(motion);
        level.addFreshEntity(pEnt);

        if(!player.isCreative()) {
            ammoStack.shrink(1);
            if (ammoStack.isEmpty())
                player.getInventory().removeItem(ammoStack);
        }

        if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
            heldStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(usedHand));

        ShootableGadgetItemMethods.applyCooldown(player, heldStack, usedHand, stack -> stack.getItem() instanceof PackageCannon, 15);
        if (player instanceof ServerPlayer serverPlayer) {
            CMPPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new PackageCannonPacket(barrelPos, lookVec.normalize(), ammoStack, usedHand, soundPitch, false));
            CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PackageCannonPacket(barrelPos, lookVec.normalize(), ammoStack, usedHand, soundPitch, true));
        }

        return InteractionResultHolder.success(heldStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            super.appendHoverText(stack, level, tooltip, flag);
            return;
        }

        Ammo ammo = getAmmo(player, stack);
        if (ammo == null) {
            super.appendHoverText(stack, level, tooltip, flag);
            return;
        }
        ItemStack ammoStack = ammo.stack();

        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(Component.translatable(ammoStack.getDescriptionId())
                .withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || newStack.getItem() != oldStack.getItem();
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return PackageItem::isPackage;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.POWER_ARROWS)
            return true;
        if (enchantment == Enchantments.PUNCH_ARROWS)
            return true;
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return BacktankUtil.isBarVisible(stack, maxUses());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return BacktankUtil.getBarWidth(stack, maxUses());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BacktankUtil.getBarColor(stack, maxUses());
    }

    private static int maxUses() {
        return CMPConfigs.server().maxPackageCannonShots.get();
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (!player.swinging){
            return HumanoidModel.ArmPose.ITEM;
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new PackageCannonRenderer()));
    }

    public record Ammo(ItemStack stack) {
    }
}
