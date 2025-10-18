package de.theidler.create_mobile_packages.items.portable_stock_ticker;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// inspired by com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem
public class LogisticallyLinkedItem extends Item {
    public static final String CMP_LLI_ITEM_TAG = "CMPLLIItemTag";

    public LogisticallyLinkedItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isTuned(stack);
    }

    public static boolean isTuned(ItemStack pStack) {
        return pStack.hasTag() && pStack.getTag()
                .contains(CMP_LLI_ITEM_TAG);
    }

    @Nullable
    public static UUID networkFromStack(ItemStack pStack) {
        if (!isTuned(pStack))
            return null;
        CompoundTag tag = pStack.getTag()
                .getCompound(CMP_LLI_ITEM_TAG);
        if (!tag.hasUUID("Freq"))
            return null;
        return tag.getUUID("Freq");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Level pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (!isTuned(pStack))
            return;

        CompoundTag tag = pStack.getTag()
                .getCompound(CMP_LLI_ITEM_TAG);
        if (!tag.hasUUID("Freq"))
            return;

        CreateLang.translate("logistically_linked.tooltip")
                .style(ChatFormatting.GOLD)
                .addTo(pTooltip);

        CreateLang.translate("logistically_linked.tooltip_clear")
                .style(ChatFormatting.GRAY)
                .addTo(pTooltip);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        player.displayClientMessage(tuned ? CreateLang.translateDirect("logistically_linked.connected")
                : CreateLang.translateDirect("logistically_linked.new_network_started"), true);
        return useOn;
    }

    public static void assignFrequency(ItemStack stack, Player player, UUID frequency) {
        CompoundTag stackTag = stack.getOrCreateTag();
        CompoundTag teTag = new CompoundTag();
        teTag.putUUID("Freq", frequency);
        stackTag.put(CMP_LLI_ITEM_TAG, teTag);

        player.displayClientMessage(CreateLang.translateDirect("logistically_linked.tuned"), true);
        stack.setTag(stackTag);
    }
}
