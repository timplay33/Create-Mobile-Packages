package de.theidler.create_mobile_packages.items.mobile_packager;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class OpenEditMenuPacket extends SimplePacketBase {

    private final ItemStack originalPackage;

    public OpenEditMenuPacket(ItemStack originalPackage) {
        this.originalPackage = originalPackage;
    }

    public OpenEditMenuPacket(FriendlyByteBuf buffer) {
        this.originalPackage = buffer.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeItem(originalPackage);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) return;

            if (player.containerMenu != null) {
                player.closeContainer();
            }

            player.getServer().tell(new net.minecraft.server.TickTask(1, () -> {
                NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, inv, p) -> new MobilePackagerEditMenu(id, inv, new MobilePackagerEdit(), originalPackage),
                        Component.translatable("item.create_mobile_packages.mobile_packager")
                ), buf -> buf.writeItem(originalPackage));
                CreateMobilePackages.LOGGER.info("Versuche EditMenu zu öffnen mit Paket: {}", originalPackage);
            }));
        });
        return true;
    }
}
