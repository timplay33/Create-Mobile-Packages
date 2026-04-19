package de.theidler.create_mobile_packages.index;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.Create;
import de.theidler.create_mobile_packages.IExtendedLogisticsNetwork;
import de.theidler.create_mobile_packages.network_settings.NetworkHelper;
import de.theidler.create_mobile_packages.robo.RoboManager;
import de.theidler.create_mobile_packages.toast.RemoveAllToastsOnClientPacket;
import de.theidler.create_mobile_packages.toast.ShowToastOnClientPacket;
import de.theidler.create_mobile_packages.toast.types.SimpleToast;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
                        .then(
                                Commands.literal("network")
                                        .requires(cs -> cs.hasPermission(2)) // admin only
                                        .then(
                                                Commands.literal("list")
                                                        .executes(CMPCommands::showAllNetworks)
                                        )
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument("player", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument("networkId", StringArgumentType.word())
                                                                                        .suggests((ctx, builder) -> {
                                                                                            Create.LOGISTICS.logisticsNetworks.forEach((uuid, value) -> {
                                                                                                IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(value);
                                                                                                String name = extendedNetwork != null ? extendedNetwork.create_mobile_packages$getName() : "Unnamed Network";
                                                                                                if (!name.equals("Unnamed Network")) {
                                                                                                    builder.suggest(uuid.toString());
                                                                                                }
                                                                                            });
                                                                                            return builder.buildFuture();
                                                                                        })
                                                                                        .executes(ctx -> addPlayerToNetwork(ctx,
                                                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                                                UUID.fromString(StringArgumentType.getString(ctx, "networkId"))))
                                                                        )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("remove")
                                                        .then(
                                                                Commands.argument("player", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument("networkId", StringArgumentType.word())
                                                                                        .suggests((ctx, builder) -> {
                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                                                            Create.LOGISTICS.logisticsNetworks.forEach((key, value) -> {
                                                                                                IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(value);
                                                                                                if (extendedNetwork != null && extendedNetwork.create_mobile_packages$getPlayers().contains(player.getUUID())) {
                                                                                                    builder.suggest(key.toString());
                                                                                                }
                                                                                            });
                                                                                            return builder.buildFuture();
                                                                                        })
                                                                                        .executes(ctx -> removePlayerFromNetwork(ctx,
                                                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                                                UUID.fromString(StringArgumentType.getString(ctx, "networkId"))))
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int showAllNetworks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        if (Create.LOGISTICS.logisticsNetworks.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No logistics networks found."), false);
            return 0;
        }

        StringBuilder output = new StringBuilder("═══════════════════════════════════\n");
        output.append("Logistics Networks:\n");

        for (var entry : Create.LOGISTICS.logisticsNetworks.entrySet()) {
            UUID networkId = entry.getKey();
            var logisticsNetwork = entry.getValue();
            IExtendedLogisticsNetwork extendedNetwork = NetworkHelper.getExtendedLogisticsNetwork(logisticsNetwork);

            String name = extendedNetwork != null ? extendedNetwork.create_mobile_packages$getName() : "Unnamed Network";
            if (name.equals("Unnamed Network")) continue; // Skip unnamed networks

            int playerCount = extendedNetwork.create_mobile_packages$getPlayers().size();
            boolean isLocked = logisticsNetwork.locked;
            Player owner = level.getPlayerByUUID(logisticsNetwork.owner);
            String ownerName = owner != null ? owner.getName().getString() : "Unknown";

            String shortId = networkId.toString().substring(0, 8);
            output.append("\n▸ ").append(name)
                    .append(" | ID: ").append(shortId).append("...")
                    .append("\n  └─ Players: ").append(playerCount)
                    .append(" | Status: ").append(isLocked ? "🔒 Locked" : "🔓 Unlocked")
                    .append(" | Owner: ").append(ownerName);
        }

        output.append("\n═══════════════════════════════════");
        String finalOutput = output.toString();
        source.sendSuccess(() -> Component.literal(finalOutput), false);
        return 1;
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

    private static int addPlayerToNetwork(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer, UUID networkId) {
        CommandSourceStack source = context.getSource();

        IExtendedLogisticsNetwork network = NetworkHelper.getExtendedLogisticsNetwork(networkId);
        if (network == null) return 0;
        network.create_mobile_packages$addPlayer(targetPlayer.getUUID());

        source.sendSuccess(() -> Component.literal("Added player " + targetPlayer.getName().getString() + " to network " + networkId), true);
        return 1;
    }

    private static int removePlayerFromNetwork(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer, UUID networkId) {
        CommandSourceStack source = context.getSource();

        IExtendedLogisticsNetwork network = NetworkHelper.getExtendedLogisticsNetwork(networkId);
        if (network == null) return 0;
        network.create_mobile_packages$removePlayer(targetPlayer.getUUID());

        source.sendSuccess(() -> Component.literal("Removed player " + targetPlayer.getName().getString() + " from network " + networkId), true);
        return 1;
    }
}
