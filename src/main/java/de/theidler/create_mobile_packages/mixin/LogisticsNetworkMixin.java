package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(value = LogisticsNetwork.class, remap = false)
public class LogisticsNetworkMixin implements CreateMobilePackages.IExtendedLogisticsNetwork {

    @Unique
    private Set<UUID> create_mobile_packages$players;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void create_mobile_packages$init(UUID id, CallbackInfo ci) {
        this.create_mobile_packages$players = new HashSet<>();
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void create_mobile_packages$writePlayers(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        tag.put(
                "CMP_Players",
                NBTHelper.writeCompoundList(
                        create_mobile_packages$players,
                        uuid -> {
                            CompoundTag t = new CompoundTag();
                            t.putUUID("UUID", uuid);
                            return t;
                        }
                )
        );
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void create_mobile_packages$readPlayers(
            CompoundTag tag,
            CallbackInfoReturnable<LogisticsNetwork> cir
    ) {
        LogisticsNetwork network = cir.getReturnValue();
        CreateMobilePackages.IExtendedLogisticsNetwork ext = (CreateMobilePackages.IExtendedLogisticsNetwork) network;

        if (tag.contains("CMP_Players", Tag.TAG_LIST)) {
            NBTHelper.iterateCompoundList(
                    tag.getList("CMP_Players", Tag.TAG_COMPOUND),
                    nbt -> ext.create_mobile_packages$addPlayer(nbt.getUUID("UUID"))
            );
        }
    }

    @Override
    public Set<UUID> create_mobile_packages$getPlayers() {
        return create_mobile_packages$players;
    }

    @Override
    public void create_mobile_packages$addPlayer(UUID player) {
        create_mobile_packages$players.add(player);
    }
}
