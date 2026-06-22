package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsNetwork;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(value = LogisticsNetwork.class, remap = false)
public abstract class LogisticsNetworkMixin implements IExtendedLogisticsNetwork {
    static {
        CreateMobilePackages.LOGGER.info("LogisticsNetworkMixin class loaded");
    }

    @Shadow
    public UUID owner;

    @Unique
    private Set<UUID> create_mobile_packages$players = new HashSet<>();
    @Unique
    private boolean create_mobile_packages$isOwnerMember = true;
    @Unique
    private String create_mobile_packages$name = "Unnamed Network";

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private static void create_mobile_packages$read(
            CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<LogisticsNetwork> cir
    ) {
        LogisticsNetwork network = cir.getReturnValue();
        if (!(network instanceof IExtendedLogisticsNetwork ext)) return;

        // Read players
        if (tag.contains("CMP_Players", Tag.TAG_LIST)) {
            NBTHelper.iterateCompoundList(
                    tag.getList("CMP_Players", Tag.TAG_COMPOUND),
                    nbt -> ext.create_mobile_packages$addPlayer(nbt.getUUID("UUID"))
            );
        }

        // Read isOwnerMember
        if (tag.contains("CMP_IsOwnerMember")) {
            ext.create_mobile_packages$setOwnerMember(tag.getBoolean("CMP_IsOwnerMember"));
        }

        // Read name
        if (tag.contains("name", Tag.TAG_STRING)) {
            ext.create_mobile_packages$setName(tag.getString("name"));
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void create_mobile_packages$init(UUID id, CallbackInfo ci) {
        // Force initialization - should already be done by field initializer, but be explicit
        if (create_mobile_packages$players == null) {
            create_mobile_packages$players = new HashSet<>();
        }
        if (create_mobile_packages$name == null) {
            create_mobile_packages$name = "Logistics Network " + id.toString().substring(0, 4);
        }
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    private void create_mobile_packages$write(CallbackInfoReturnable<CompoundTag> cir) {
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
        tag.putBoolean("CMP_IsOwnerMember", create_mobile_packages$isOwnerMember);
        tag.putString("name", create_mobile_packages$name);
    }

    @Override
    public Set<UUID> create_mobile_packages$getPlayers() {
        return create_mobile_packages$players;
    }

    @Override
    public void create_mobile_packages$addPlayer(UUID player) {
        if (player.equals(owner)) {
            create_mobile_packages$isOwnerMember = true;
            Create.LOGISTICS.markDirty();
            return;
        }
        create_mobile_packages$players.add(player);
        Create.LOGISTICS.markDirty();
    }

    @Override
    public void create_mobile_packages$removePlayer(UUID player) {
        if (player.equals(owner)) {
            create_mobile_packages$isOwnerMember = false;
            Create.LOGISTICS.markDirty();
            return;
        }
        create_mobile_packages$players.remove(player);
        Create.LOGISTICS.markDirty();
    }

    @Override
    public boolean create_mobile_packages$isOwnerMember() {
        return create_mobile_packages$isOwnerMember;
    }

    @Override
    public void create_mobile_packages$setOwnerMember(boolean isMember) {
        this.create_mobile_packages$isOwnerMember = isMember;
        Create.LOGISTICS.markDirty();
    }

    @Override
    public boolean create_mobile_packages$isPlayerMember(UUID playerUuid) {
        return create_mobile_packages$players.contains(playerUuid) ||
                (create_mobile_packages$isOwnerMember && playerUuid.equals(owner));
    }

    @Override
    public String create_mobile_packages$getName() {
        return create_mobile_packages$name;
    }

    @Override
    public void create_mobile_packages$setName(String name) {
        this.create_mobile_packages$name = name;
        Create.LOGISTICS.markDirty();
    }
}
