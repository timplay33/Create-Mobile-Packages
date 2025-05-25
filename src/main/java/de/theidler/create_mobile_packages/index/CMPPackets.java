package de.theidler.create_mobile_packages.index;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_portal.RequestDimensionTeleport;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.BigItemStackListPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.HiddenCategoriesPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.RequestStockUpdate;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.SendPackage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public enum CMPPackets {
    // Client to Server
    REQUEST_DIMENSION_TELEPORT(RequestDimensionTeleport.class, RequestDimensionTeleport::new, PLAY_TO_SERVER),
    LOGISTICS_PACKAGE_REQUEST(SendPackage.class, SendPackage::new, PLAY_TO_SERVER),
    REQUEST_STOCK_UPDATE(RequestStockUpdate.class, RequestStockUpdate::new, PLAY_TO_SERVER),
    HIDDEN_CATEGORIES(HiddenCategoriesPacket.class, HiddenCategoriesPacket::new, PLAY_TO_SERVER),

    // Server to Client
    BIG_ITEM_STACK_LIST(BigItemStackListPacket.class, BigItemStackListPacket::read, NetworkDirection.PLAY_TO_CLIENT);


    public static final ResourceLocation CHANNEL_NAME = CreateMobilePackages.asResource("main");
    public static final int NETWORK_VERSION = 3;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    private static SimpleChannel channel;

    private final CMPPackets.PacketType<?> packetType;

    <T extends SimplePacketBase> CMPPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
                                            NetworkDirection direction) {
        packetType = new CMPPackets.PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
        channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
                .serverAcceptedVersions(NETWORK_VERSION_STR::equals)
                .clientAcceptedVersions(NETWORK_VERSION_STR::equals)
                .networkProtocolVersion(() -> NETWORK_VERSION_STR)
                .simpleChannel();

        for (CMPPackets packet : values())
            packet.packetType.register();
    }

    public static SimpleChannel getChannel() {
        return channel;
    }

    public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
        getChannel().send(
                PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.dimension())),
                message);
    }

    private static class PacketType<T extends SimplePacketBase> {
        private static int index = 0;

        private final BiConsumer<T, FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private final Class<T> type;
        private final NetworkDirection direction;

        private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
            encoder = T::write;
            decoder = factory;
            handler = (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (packet.handle(context)) {
                    context.setPacketHandled(true);
                }
            };
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            getChannel().messageBuilder(type, index++, direction)
                    .encoder(encoder)
                    .decoder(decoder)
                    .consumerNetworkThread(handler)
                    .add();
        }
    }
}
