package de.theidler.create_mobile_packages.index;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.theidler.create_mobile_packages.toast.CustomToast;
import de.theidler.create_mobile_packages.toast.RemoveAllToastsOnClientPacket;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class CMPCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cmp")
                        .requires(cs -> cs.hasPermission(2)) // admin only
                        .then(
                                Commands.literal("toast")
                                        .then(
                                                Commands.literal("clear")
                                                .executes(CMPCommands::clearToasts)
                                        )
                                        .then(
                                               Commands.literal("create")
                                                       .then(
                                                               Commands.argument("message", StringArgumentType.greedyString())
                                                               .executes(ctx -> {
                                                                   String message = StringArgumentType.getString(ctx, "message");
                                                                   return createToast(ctx, message);
                                                               })
                                                       )
                                        )
                        )
        );
    }

    private static int createToast(CommandContext<CommandSourceStack> context, String message) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        CustomToast toast = new CustomToast(UUID.randomUUID(), Component.literal(message), Component.literal(""), CMPItems.ROBO_BEE.asStack());
        CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new ShowToastOnClientPacket(toast));
        return 1;
    }

    private static int clearToasts(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        CMPPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new RemoveAllToastsOnClientPacket());
        return 1;
    }
}
