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

@SuppressWarnings("UnstableApiUsage")
public class GenericStackListPacket extends SimplePacketBase {

    private final List<GenericStack> stacks;

    // Standard constructor
    public GenericStackListPacket(List<GenericStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(stacks.size());
        for (GenericStack stack : stacks) {
            GenericStackSerializer.write(stack, buffer);
        }
    }

    public static GenericStackListPacket read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<GenericStack> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(GenericStackSerializer.read(buffer));
        }
        return new GenericStackListPacket(list);
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
