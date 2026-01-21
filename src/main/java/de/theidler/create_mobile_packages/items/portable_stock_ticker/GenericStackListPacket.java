package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;

import java.util.ArrayList;
import java.util.List;

public class GenericStackListPacket extends SimplePacketBase {

    private final List<GenericStack> stacks;
    private final boolean isLast;

    // Standard constructor
    public GenericStackListPacket(List<GenericStack> stacks, boolean isLast) {
        this.stacks = stacks;
        this.isLast = isLast;
    }

    public static GenericStackListPacket read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<GenericStack> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(GenericStackSerializer.read(buffer));
        }
        boolean isLast = buffer.readBoolean();
        return new GenericStackListPacket(list, isLast);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(stacks.size());
        for (GenericStack stack : stacks) {
            GenericStackSerializer.write(stack, buffer);
        }
        buffer.writeBoolean(isLast);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(this::handleClient);
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        ClientScreenStorage.receiveChunk(stacks, isLast);
    }

}
