package de.theidler.create_mobile_packages.index;

import com.mojang.serialization.Codec;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class CMPDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateMobilePackages.MODID);

    public static final DataComponentType<CustomData> CMP_FREQ = register(
            "cmp_freq",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
    );

    public static final DataComponentType<String> ADDRESS_TAG = register(
            "address_tag",
            builder -> builder.persistent(Codec.STRING)
    );

    public static final DataComponentType<List<ItemStack>> CATEGORIES = register(
            "categories",
            builder -> builder
                    .persistent(ItemStack.CODEC.listOf())
    );

    public static final DataComponentType<Map<UUID, List<Integer>>> HIDDEN_CATEGORIES = register(
            "hidden_categories",
            builder -> builder
                    .persistent(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT.listOf()))
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
