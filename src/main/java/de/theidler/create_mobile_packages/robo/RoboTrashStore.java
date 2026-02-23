package de.theidler.create_mobile_packages.robo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoboTrashStore {
    private final UUID playerUUID;
    private final UUID networkUUID;
    private final List<ItemStack> itemStacks;
    private String targetAddress = "";

    public RoboTrashStore(UUID playerUUID, UUID networkUUID, List<ItemStack> itemStacks) {
        this.playerUUID = playerUUID;
        this.networkUUID = networkUUID;
        this.itemStacks = itemStacks;
    }

    public static RoboTrashStore load(CompoundTag tag) {
        UUID playerUUID = tag.getUUID("PlayerUUID");
        UUID networkUUID = tag.getUUID("NetworkUUID");
        ListTag itemsTag = tag.getList("ItemStacks", Tag.TAG_COMPOUND);
        List<ItemStack> itemStacks = new ArrayList<>(itemsTag.stream()
                .map(itemTag -> ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().orElse(ItemStack.EMPTY))
                .toList());

        RoboTrashStore store = new RoboTrashStore(playerUUID, networkUUID, itemStacks);
        if (tag.contains("TargetAddress")) {
            store.setTargetAddress(tag.getString("TargetAddress"));
        }
        return store;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String address) {
        this.targetAddress = address;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getNetworkUUID() {
        return networkUUID;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    /**
     * Returns true if at least one slot contains a non-empty ItemStack.
     */
    public boolean hasItems() {
        return itemStacks.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putUUID("NetworkUUID", networkUUID);
        ListTag itemsTag = new ListTag();
        itemStacks.forEach(itemStack -> itemsTag.add(ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, itemStack).result().orElse(new CompoundTag())));
        tag.put("ItemStacks", itemsTag);
        tag.putString("TargetAddress", targetAddress);
        return tag;
    }
}
