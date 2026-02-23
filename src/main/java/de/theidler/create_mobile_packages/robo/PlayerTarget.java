package de.theidler.create_mobile_packages.robo;

import com.simibubi.create.content.logistics.box.PackageItem;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.blocks.bee_port.RoboRequest;
import de.theidler.create_mobile_packages.index.CMPItems;
import de.theidler.create_mobile_packages.network_settings.NetworkHelper;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import de.theidler.create_mobile_packages.toast.Toast;
import de.theidler.create_mobile_packages.toast.types.PackageToast;
import de.theidler.create_mobile_packages.toast.types.SimpleToast;
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
import java.util.UUID;

import static de.theidler.create_mobile_packages.CMPHelper.doesAddressMatchPlayer;

public class PlayerTarget implements RoboTarget {
    private final Player player;
    private int eta;
    private final IExtendedLogisticsNetwork network;
    private long lastToastUpdate = 0;

    public PlayerTarget(Player player, UUID networkId) {
        this.player = player;
        this.network = NetworkHelper.getExtendedLogisticsNetwork(networkId);
    }

    public static @Nullable PlayerTarget fromAddress(ServerLevel level, String address, UUID networkId) {
        IExtendedLogisticsNetwork network = NetworkHelper.getExtendedLogisticsNetwork(networkId);
        if (network == null) return null;
        ServerPlayer player = level.getPlayers((p) -> doesAddressMatchPlayer(p, address)).stream().filter(p -> network.create_mobile_packages$getPlayers().contains(p.getUUID())).findFirst().orElse(null);
        if (player == null) return null;
        return new PlayerTarget(player, networkId);
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
        return player != null && player.isAlive() && network != null && network.create_mobile_packages$getPlayers().contains(player.getUUID());
    }

    public void updateEtaToast(VirtualRobo robo) {
        // update only every Second
        if (System.currentTimeMillis() - lastToastUpdate < 1000) return;
        ItemStack packageItem = robo.getItemStack();
        ItemStackHandler itemHandler = packageItem.isEmpty() ? new ItemStackHandler(9) : PackageItem.getContents(packageItem);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            items.add(itemHandler.getStackInSlot(i));
        }

        Toast toast = null;

        RoboRequest.Mission missionType = robo.getRequest() != null ? robo.getRequest().getMission() : RoboRequest.Mission.DELIVER;
        switch (missionType) {
            case DELIVER -> toast = new PackageToast(
                    robo.getId(),
                    Component.translatable("create_mobile_packages.toast.robo_bee_on_the_way"),
                    Component.translatable("create_mobile_packages.toast.eta", getETA()),
                    CMPItems.ROBO_BEE.asStack(),
                    items
            );
            case PICKUP -> toast = new SimpleToast(
                    robo.getId(),
                    Component.translatable("create_mobile_packages.toast.robo_bee_on_the_way"),
                    Component.translatable("create_mobile_packages.toast.eta", getETA()),
                    CMPItems.ROBO_BEE.asStack()
            );
        }

        if (player instanceof ServerPlayer serverPlayer && toast != null)
            CatnipServices.NETWORK.sendToClient(serverPlayer, new ShowToastOnClientPacket(toast));
        lastToastUpdate = System.currentTimeMillis();
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
