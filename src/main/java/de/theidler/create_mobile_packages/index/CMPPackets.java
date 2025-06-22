package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.*;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum CMPPackets implements BasePacketPayload.PacketTypeProvider {
    // Client to Server
    LOGISTICS_PACKAGE_REQUEST(SendPackage.class, SendPackage.STREAM_CODEC),
    REQUEST_STOCK_UPDATE(RequestStockUpdate.class, RequestStockUpdate.STREAM_CODEC),
    HIDDEN_CATEGORIES(HiddenCategoriesPacket.class, HiddenCategoriesPacket.STREAM_CODEC),
    OPEN_PORTABLE_STOCK_TICKER(OpenPortableStockTicker.class, OpenPortableStockTicker.STREAM_CODEC),

    // Server to Client
    BIG_ITEM_STACK_LIST(GenericStackListPacket.class, GenericStackListPacket.STREAM_CODEC);


    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> CMPPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(CreateMobilePackages.asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateMobilePackages.MODID, 1);
        for (CMPPackets packet : CMPPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }

}
