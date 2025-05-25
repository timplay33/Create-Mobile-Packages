package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class BigItemStackListPacket extends SimplePacketBase {

    private final List<BigItemStack> stacks;

    // Standard constructor
    public BigItemStackListPacket(List<BigItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(stacks.size());
        for (BigItemStack stack : stacks) {
            stack.send(buffer);
        }
    }

    public static BigItemStackListPacket read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<BigItemStack> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(BigItemStack.receive(buffer));
        }
        return new BigItemStackListPacket(list);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        ClientScreenStorage.stacks = stacks;
    }
}
