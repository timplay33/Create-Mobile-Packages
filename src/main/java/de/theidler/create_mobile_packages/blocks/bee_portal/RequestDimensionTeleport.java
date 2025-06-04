package de.theidler.create_mobile_packages.blocks.bee_portal;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.BeePortalConnection;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

public class RequestDimensionTeleport extends SimplePacketBase {
    private final ServerLevel targetLevel;
    private final Vector3f originPos;
    private final BlockPos targetPos;
    private final ItemStack itemStack;

    public RequestDimensionTeleport(ResourceLocation resLocation, Vec3 originPos, BlockPos targetPos, ItemStack itemStack) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null)
            this.targetLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, resLocation));
        else
            this.targetLevel = null; // TODO: Make it work for multiplayer

        this.originPos = originPos.toVector3f();
        this.targetPos = targetPos;
        this.itemStack = itemStack;
    }

    public RequestDimensionTeleport(FriendlyByteBuf buffer) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null)
            targetLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation()));
        else
            targetLevel = null; // TODO: Make it work for multiplayer

        originPos = buffer.readVector3f();
        targetPos = buffer.readBlockPos();
        itemStack = buffer.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(targetLevel.dimension().location());
        buffer.writeVector3f(originPos);
        buffer.writeBlockPos(targetPos);
        buffer.writeItem(itemStack);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (targetLevel != null) {
                BeePortStorage storage = BeePortStorage.get(targetLevel);
                BeePortalConnection portalConnection = storage.getPortalConnection(BlockPos.containing(new Vec3(originPos)), new Location(targetPos, targetLevel));
                BeePortalBlockEntity exitPortal = portalConnection.getCurrent(targetLevel);
//                        storage.getClosestBeePortal(BlockPos.containing(new Vec3(spawnPos)), targetLevel);
                if (exitPortal != null) {
                    RoboBeeEntity drone = new RoboBeeEntity(targetLevel, itemStack, new Location(targetPos, targetLevel), exitPortal.getBlockPos());
                    drone.setPackageHeightScale(1f);
                    targetLevel.addFreshEntity(drone);
                }
            }
        });

        return true;
    }
}
