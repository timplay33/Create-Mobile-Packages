package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.blocks.BeePortStorage;
import de.theidler.create_mobile_packages.blocks.bee_portal.BeePortalBlockEntity;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import de.theidler.create_mobile_packages.entities.robo_entity.Location;
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

import java.util.Comparator;
import java.util.List;

public class RequestDimensionTeleport extends SimplePacketBase {
    private final ServerLevel serverLevel;
    private final Vector3f spawnPos;
    private final BlockPos targetPos;
    private final ItemStack itemStack;

    public RequestDimensionTeleport(ResourceLocation resLocation, Vec3 spawnPos, BlockPos targetPos, ItemStack itemStack) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null)
            this.serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, resLocation));
        else
            this.serverLevel = null; // TODO: Make it work for multiplayer

        this.spawnPos = spawnPos.toVector3f();
        this.targetPos = targetPos;
        this.itemStack = itemStack;
    }

    public RequestDimensionTeleport(FriendlyByteBuf buffer) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null)
            serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation()));
        else
            serverLevel = null; // TODO: Make it work for multiplayer

        spawnPos = buffer.readVector3f();
        targetPos = buffer.readBlockPos();
        itemStack = buffer.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(serverLevel.dimension().location());
        buffer.writeVector3f(spawnPos);
        buffer.writeBlockPos(targetPos);
        buffer.writeItem(itemStack);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (serverLevel != null) {
                BlockPos spawnBlockPos = new BlockPos(Math.round(spawnPos.x()), Math.round(spawnPos.y()), Math.round(spawnPos.z()));
                BeePortalBlockEntity exitPortal = BeePortStorage.getPortals(serverLevel).stream()
                        .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(spawnBlockPos)))
                        .orElse(null);
                if (exitPortal != null) {
                    RoboBeeEntity drone = new RoboBeeEntity(serverLevel, itemStack, new Location(targetPos, serverLevel.dimensionType()), exitPortal.getBlockPos());
                    drone.setPackageHeightScale(1f);
                    serverLevel.addFreshEntity(drone);
                }
            }
        });

        return true;
    }
}
