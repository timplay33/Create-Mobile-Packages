package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.AdventureUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class SendPackage extends SimplePacketBase {
    private final PackageOrderWithCrafts order;
    private final String address;
    private final boolean encodeRequester;

    public SendPackage(PackageOrderWithCrafts order, String address, boolean encodeRequester) {
        this.order = order;
        this.address = address;
        this.encodeRequester = encodeRequester;
    }

    public SendPackage(FriendlyByteBuf buffer) {
        address = buffer.readUtf();
        // Read the order from the buffer
        int orderSize = buffer.readVarInt();
        List<BigItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < orderSize; i++) {
            stacks.add(new BigItemStack(
                buffer.readItem(),
                buffer.readVarInt()
            ));
        }
        // Read the crafting information from the buffer
        int craftingInfoSize = buffer.readVarInt();
        List<PackageOrderWithCrafts.CraftingEntry> craftingInformation = new ArrayList<>();
        for (int i = 0; i < craftingInfoSize; i++) {
            craftingInformation.add(
                PackageOrderWithCrafts.CraftingEntry.read(buffer)
            );
        }
        // Create the PackageOrderWithCrafts object
        order = new PackageOrderWithCrafts(
            new PackageOrder(stacks),
                craftingInformation
        );
        encodeRequester = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(address);
        buffer.writeVarInt(order.orderedStacks().stacks().size());
        order.orderedStacks().stacks().forEach(stack -> {
            buffer.writeItem(stack.stack);
            buffer.writeVarInt(stack.count);
        });
        buffer.writeVarInt(order.orderedCrafts().size());
        order.orderedCrafts().forEach(stack -> {
            stack.write(buffer);
        });
        buffer.writeBoolean(encodeRequester);
    }


    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
                return;
            Level world = player.level();
            if (!world.isLoaded(player.blockPosition()))
                return;
            applySettings(player);
        });
        return true;
    }

    protected void applySettings(ServerPlayer player) {

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(player.level(), player.blockPosition());
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), player.blockPosition());
        }

        ItemStack pstStack = PortableStockTicker.find(player.getInventory());
        PortableStockTicker pst = pstStack != null ? (PortableStockTicker) pstStack.getItem() : null;
        if (pst != null)
            pst.broadcastPackageRequest(
                LogisticallyLinkedBehaviour.RequestType.PLAYER,
                order,
                null,
                address,
                player
            );
    }
}
