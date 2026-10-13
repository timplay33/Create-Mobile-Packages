package de.theidler.create_mobile_packages.mixin;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import de.theidler.create_mobile_packages.network_settings.ClientNetworkDataStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RedstoneRequesterBlockItem.class, remap = false)
public class RedstoneRequesterBlockItemMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void cmp$appendNetworkName(ItemStack stack, Item.TooltipContext tooltipContext,
                                       List<Component> tooltipComponents, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!stack.has(AllDataComponents.AUTO_REQUEST_DATA)) return;
        ClientNetworkDataStorage.appendNetworkName(tooltipComponents, LogisticallyLinkedBlockItem.networkFromStack(stack));
    }
}
