package de.theidler.create_mobile_packages.index;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.ToggleBeeReturnModePacket;
import de.theidler.create_mobile_packages.blocks.bee_port.ToggleFilterModePacket;
import de.theidler.create_mobile_packages.items.mobile_packager.ConfirmEditMenuPacket;
import de.theidler.create_mobile_packages.items.mobile_packager.OpenEditMenuPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.*;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.OpenTrashMenuPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.SyncTrashAddressPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.SyncTrashAddressToClientPacket;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.trash_menu.SyncTrashItemsToClientPacket;
import de.theidler.create_mobile_packages.network_settings.*;
import de.theidler.create_mobile_packages.toast.RemoveAllToastsOnClientPacket;
import de.theidler.create_mobile_packages.toast.RemoveToastOnClientPacket;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
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
    OPEN_EDIT_MENU(OpenEditMenuPacket.class, OpenEditMenuPacket.STREAM_CODEC),
    CONFIRM_EDIT_MENU(ConfirmEditMenuPacket.class, ConfirmEditMenuPacket.STREAM_CODEC),
    SET_NETWORK_NAME(SetNetworkNamePackage.class, SetNetworkNamePackage.STREAM_CODEC),
    ADD_PLAYER_TO_NETWORK(AddPlayerToNetworkPackage.class, AddPlayerToNetworkPackage.STREAM_CODEC),
    REMOVE_PLAYER_FROM_NETWORK(RemovePlayerFromNetworkPackage.class, RemovePlayerFromNetworkPackage.STREAM_CODEC),
    MODIFY_NETWORK_LOCK_STATE(ModifyNetworkLockStatePackage.class, ModifyNetworkLockStatePackage.STREAM_CODEC),
    REQUEST_NETWORK_DATA(RequestNetworkDataPacket.class, RequestNetworkDataPacket.STREAM_CODEC),
    REQUEST_PLAYER_NETWORKS(RequestPlayerNetworksPacket.class, RequestPlayerNetworksPacket.STREAM_CODEC),
    OPEN_TRASH_MENU(OpenTrashMenuPacket.class, OpenTrashMenuPacket.STREAM_CODEC),
    SYNC_TRASH_ADDRESS(SyncTrashAddressPacket.class, SyncTrashAddressPacket.STREAM_CODEC),
    SAVE_PORTABLE_STOCK_TICKER_ADDRESS(SavePortableStockTickerAddressPacket.class, SavePortableStockTickerAddressPacket.STREAM_CODEC),
    TOGGLE_BEE_RETURN_MODE(ToggleBeeReturnModePacket.class, ToggleBeeReturnModePacket.STREAM_CODEC),
    TOGGLE_FILTER_MODE(ToggleFilterModePacket.class, ToggleFilterModePacket.STREAM_CODEC),

    // Server to Client
    BIG_ITEM_STACK_LIST(GenericStackListPacket.class, GenericStackListPacket.STREAM_CODEC),
    SYNC_TRASH_ADDRESS_TO_CLIENT(SyncTrashAddressToClientPacket.class, SyncTrashAddressToClientPacket.STREAM_CODEC),
    SYNC_TRASH_ITEMS_TO_CLIENT(SyncTrashItemsToClientPacket.class, SyncTrashItemsToClientPacket.STREAM_CODEC),
    SHOW_TOAST_ON_CLIENT(ShowToastOnClientPacket.class, ShowToastOnClientPacket.STREAM_CODEC),
    REMOVE_TOAST_ON_CLIENT(RemoveToastOnClientPacket.class, RemoveToastOnClientPacket.STREAM_CODEC),
    REMOVE_ALL_TOAST_ON_CLIENT(RemoveAllToastsOnClientPacket.class, RemoveAllToastsOnClientPacket.STREAM_CODEC),
    NETWORK_DATA(NetworkDataPacket.class, NetworkDataPacket.STREAM_CODEC),
    CLEAR_NETWORKS(ClearNetworksPacket.class, ClearNetworksPacket.STREAM_CODEC);


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
