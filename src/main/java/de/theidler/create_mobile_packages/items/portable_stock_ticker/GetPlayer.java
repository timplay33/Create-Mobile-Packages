package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class GetPlayer extends SimplePacketBase {
    private final String playerTag;
    private final UUID beeId;

    public GetPlayer(String playerTag, UUID beeId) {
        this.playerTag = playerTag;
        this.beeId = beeId;
    }

    public GetPlayer(FriendlyByteBuf buffer) {
        playerTag = buffer.readUtf();
        beeId = buffer.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(playerTag);
        buffer.writeUUID(beeId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                Level senderLevel = sender.level();
                IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                if (server != null) {
                    // TODO: Add support for multiplayer
                    AtomicReference<Player> targetPlayer = new AtomicReference<>(null);
                    server.getAllLevels().forEach(serverLevel -> {
                        if (targetPlayer.get() == null) {
                            targetPlayer.set(serverLevel.players().stream()
                                    .filter(p -> p.getName().getString().equals(playerTag))
                                    .findFirst().orElse(null));
                        }
                    });

                    if (targetPlayer.get() != null) {
                        Level targetLevel = targetPlayer.get().level();
                        CMPPackets.getChannel()
                                .send(PacketDistributor.PLAYER.with(() -> sender), new SendPlayer(targetLevel, playerTag, beeId));
                    }
                }
            }
        });

        return true;
    }


}
