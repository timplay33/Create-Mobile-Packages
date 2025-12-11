package de.theidler.create_mobile_packages.robo;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.toast.types.PackageToast;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.theidler.create_mobile_packages.CMPHelper.doesAddressMatchPlayer;

public class PlayerTarget implements RoboTarget {
    private final Player player;
    private int eta;

    public PlayerTarget(Player player) {
        this.player = player;
    }

    public static PlayerTarget fromAddress(ServerLevel level, String address) {
        ServerPlayer player = level.getPlayers((p) -> doesAddressMatchPlayer(p, address)).stream().findFirst().orElse(null);
        return new PlayerTarget(player);
    }

    @Override
    public @Nullable Vec3 getTargetPos() {
        if (player == null) return null;
        return player.position();
    }

    @Override
    public Player asPlayer() {
        return player;
    }

    @Override
    public boolean isValid() {
        return player != null && player.isAlive();
    }

    public void updateEtaToast(VirtualRobo robo) {
        ItemStack packageItem = robo.getItemStack();
        ItemStackHandler itemHandler = packageItem.isEmpty() ? new ItemStackHandler(9) : PackageItem.getContents(packageItem);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            items.add(itemHandler.getStackInSlot(i));
        }

        PackageToast toast = new PackageToast(
                robo.getId(),
                Component.translatable("create_mobile_packages.toast.robo_bee_on_the_way"),
                Component.translatable("create_mobile_packages.toast.eta", getETA()),
                CMPItems.ROBO_BEE.asStack(),
                items
        );
        if (player instanceof ServerPlayer serverPlayer)
            CatnipServices.NETWORK.sendToClient(serverPlayer, new ShowToastOnClientPacket(toast));
    }

    @Override
    public int getETA() {
        return eta;
    }

    @Override
    public void setETA(int eta) {
        this.eta = eta;
    }
}
