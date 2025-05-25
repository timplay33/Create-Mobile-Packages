package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.entities.robo_entity.RoboEntity;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class SendPlayer extends SimplePacketBase {
    private final Player player;
    private final Level level;
    private final String playerTag;
    private final UUID beeId;
    private ServerPlayer sender = null;

    // Standard constructor
    public SendPlayer(Level level, String playerTag, UUID beeId) {
        this.level = level;
        this.playerTag = playerTag;
        this.beeId = beeId;
        this.player = level.players().stream().filter(p -> p.getName()
                .getString().equals(playerTag)).findFirst().orElse(null);
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(level.dimension().location());
        buffer.writeUtf(playerTag);
        buffer.writeUUID(beeId);
    }

    public static SendPlayer read(FriendlyByteBuf buffer) {
        String playerTag = buffer.readUtf();
        UUID beeId = buffer.readUUID();
        ResourceLocation playerLoc = buffer.readResourceLocation();
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, playerLoc));
            if (serverLevel != null) {
                Player player = serverLevel.players().stream().filter(p -> p.getName()
                        .getString().equals(playerTag)).findFirst().orElse(null);
                return new SendPlayer(serverLevel, playerTag, beeId);
            }
        }

        return null; // TODO: Make it work for multiplayer
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        sender = context.getSender();
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null)
            return; // TODO: Add support for multiplayer

        if (sender == null)
            return;

        BlockPos senderPos = sender.blockPosition();
        RoboEntity re = (RoboEntity) sender.level()
                .getEntitiesOfClass(RoboEntity.class, new AABB(senderPos), entity -> entity.getUUID().equals(beeId))
                .stream().findFirst().orElse(null);
        if (re != null)
            re.setTargetPlayer(player);
    }
}
