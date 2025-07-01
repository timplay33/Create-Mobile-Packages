package de.theidler.create_mobile_packages.compat;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

public class FactoryAbstractions {
    public static final StreamCodec<RegistryFriendlyByteBuf, GenericOrder> GENERIC_ORDER_STREAM_CODEC =
            StreamCodec.of((buffer, order) -> order.write(buffer), GenericOrder::read);

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack> GENERIC_STACK_STREAM_CODEC =
            StreamCodec.of((buffer, stack) -> GenericStackSerializer.write(stack, buffer), GenericStackSerializer::read);
}