package de.theidler.create_mobile_packages.entities.robo_entity.states;

import de.theidler.create_mobile_packages.CMPHelper;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntityState;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.index.CMPPackets;
import de.theidler.create_mobile_packages.index.config.CMPConfigs;
import de.theidler.create_mobile_packages.robo.VirtualRobo;
import de.theidler.create_mobile_packages.toast.CustomToast;
import de.theidler.create_mobile_packages.toast.RemoveToastOnClientPacket;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class FlyToTargetState implements RoboEntityState {
    private final UUID toastId = UUID.randomUUID();
    @Override
    public void tick(VirtualRobo re) {
        BlockPos targetPos = re.getTargetPosition();
        if (targetPos == null) { return; }
        if (re.getCurrentPos().distanceTo(targetPos.getCenter()) <= CMPConfigs.server().beeSpeed.get()/12.0) {
            if (re.getTargetPlayer() != null) {
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(
                        () -> (ServerPlayer) re.getTargetPlayer()),
                        new RemoveToastOnClientPacket(toastId));
                re.setState(new InteractWithPlayerState());
            } else if (re.getTargetBlockEntity() != null) {
                re.setState(new LandingPrepareState());
            }
            re.setTargetVelocity(Vec3.ZERO);
        } else {
            if (re.getTargetPlayer() instanceof ServerPlayer serverPlayer) {
                CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ShowToastOnClientPacket(
                        new CustomToast(
                                toastId,
                                Component.translatable("create_mobile_packages.toast.robo_bee_on_the_way"),
                                Component.translatable("create_mobile_packages.toast.eta", CMPHelper.calcETA(serverPlayer.position(), re.getCurrentPos())),
                                CMPItems.ROBO_BEE.asStack()
                        )
                ));
            }
            Vec3 direction = targetPos.getCenter().subtract(re.getCurrentPos()).normalize();
            double speed = CMPConfigs.server().beeSpeed.get() / 20.0;
            re.setTargetVelocity(direction.scale(speed));
            if (re.getCurrentPos().distanceTo(targetPos.getCenter()) > 2.5) { // entity rotation starts drifting
                re.lookAtTarget();
            }
        }


    }
}
