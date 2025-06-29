package de.theidler.create_mobile_packages.items.package_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetRenderHandler;
import com.simibubi.create.foundation.particle.AirParticleData;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PackageCannonRenderHandler extends ShootableGadgetRenderHandler {

    private float nextPitch;

    @Override
    protected void playSound(InteractionHand hand, Vec3 position) {
        AllSoundEvents.FWOOMP.playAt(Minecraft.getInstance().level, position, 1, nextPitch, true);
    }

    @Override
    protected boolean appliesTo(ItemStack stack) {
        return stack.getItem() instanceof PackageCannon;
    }

    public void beforeShoot(float nextPitch, Vec3 location, Vec3 motion, ItemStack stack) {
        this.nextPitch = nextPitch;
        if (stack.isEmpty())
            return;
        ClientLevel world = Minecraft.getInstance().level;
        for (int i = 0; i < 2; i++) {
            Vec3 m = VecHelper.offsetRandomly(motion.scale(0.1f), world.random, .025f);
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), location.x, location.y, location.z, m.x,
                    m.y, m.z);

            Vec3 m2 = VecHelper.offsetRandomly(motion.scale(2f), world.random, .5f);
            world.addParticle(new AirParticleData(1, 1 / 4f), location.x, location.y, location.z, m2.x, m2.y, m2.z);
        }
    }

    @Override
    protected void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        ms.translate(flip * -.1f, 0, .14f);
        ms.scale(.75f, .75f, .75f);
        TransformStack.of(ms)
                .rotateXDegrees(recoil * 80);
    }

    @Override
    protected void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
        ms.translate(flip * -.09, -.275, -.25);
        TransformStack.of(ms)
                .rotateZDegrees(flip * -10);
    }
}
