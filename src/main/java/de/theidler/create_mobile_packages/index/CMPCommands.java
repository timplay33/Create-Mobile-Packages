package de.theidler.create_mobile_packages.index;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.toast.types.SimpleToast;
import de.theidler.create_mobile_packages.toast.RemoveAllToastsOnClientPacket;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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
                        .then(
                                Commands.literal("robos")
                                        .requires(cs -> cs.hasPermission(2)) // admin only
                                        .then(
                                                Commands.literal("clear")
                                                        .executes(CMPCommands::clearRobos)
                                        )
                        )
        );
    }

    private static int clearRobos(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        RoboManager manager = RoboManager.get(level);
        int roboCount = manager.robos.size();
        manager.robos.clear();
        source.sendSuccess(() -> Component.literal("Cleared " + roboCount + " robos"), true);
        return 1;
    }

    private static int createToast(CommandContext<CommandSourceStack> context, String message) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SimpleToast toast = new SimpleToast(UUID.randomUUID(), Component.literal(message), Component.literal(""), CMPItems.ROBO_BEE.asStack());
        CatnipServices.NETWORK.sendToClient(player, new ShowToastOnClientPacket(toast));
        return 1;
    }

    private static int clearToasts(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        CatnipServices.NETWORK.sendToClient(player, RemoveAllToastsOnClientPacket.INSTANCE);
        return 1;
    }
}
