package de.theidler.create_mobile_packages.items.package_cannon;

import com.simibubi.create.content.equipment.zapper.ShootGadgetPacket;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetRenderHandler;
import de.theidler.create_mobile_packages.CreateMobilePackagesClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PackageCannonPacket extends ShootGadgetPacket {

    private float pitch;
    private Vec3 motion;
    private ItemStack item;

    public PackageCannonPacket(Vec3 location, Vec3 motion, ItemStack item, InteractionHand hand, float pitch, boolean self) {
        super(location, hand, self);
        this.pitch = pitch;
        this.motion = motion;
        this.item = item;
    }

    public PackageCannonPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    protected void readAdditional(FriendlyByteBuf buffer) {
        pitch = buffer.readFloat();
        motion = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        item = buffer.readItem();
    }

    @Override
    protected void writeAdditional(FriendlyByteBuf buffer) {
        buffer.writeFloat(pitch);
        buffer.writeFloat((float) motion.x);
        buffer.writeFloat((float) motion.y);
        buffer.writeFloat((float) motion.z);
        buffer.writeItem(item);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void handleAdditional() {
        CreateMobilePackagesClient.PACKAGE_CANNON_RENDER_HANDLER.beforeShoot(pitch, location, motion, item);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected ShootableGadgetRenderHandler getHandler() {
        return CreateMobilePackagesClient.PACKAGE_CANNON_RENDER_HANDLER;
    }
}
