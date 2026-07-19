package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandHandler {

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (OnlyTPConfig.isDisabled()) return;

        ParseResults<CommandSourceStack> parseResults = event.getParseResults();
        CommandSourceStack source = parseResults.getContext().getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        String playerName = player.getName().getString();
        boolean isOp = source.hasPermission(2);

        if (OnlyTPConfig.isBothMode()) {
            // 同时启用：黑名单分别应用
            if (!OnlyTPConfig.blacklistAllowTp.contains(playerName)) {
                handleAllowTpOnly(event, parseResults, player, isOp);
            }
            if (!OnlyTPConfig.blacklistBlockNonTp.contains(playerName)) {
                handleBlockNonTp(event, parseResults, player, isOp);
            }
        } else {
            // 黑名单：完全豁免
            if (OnlyTPConfig.isBlacklisted(playerName)) return;

            if (OnlyTPConfig.isAllowTpOnlyMode()) {
                handleAllowTpOnly(event, parseResults, player, isOp);
            } else if (OnlyTPConfig.isBlockNonTpMode()) {
                handleBlockNonTp(event, parseResults, player, isOp);
            }
        }
    }

    private void handleAllowTpOnly(CommandEvent event, ParseResults<CommandSourceStack> parse,
                                   ServerPlayer player, boolean isOp) {
        if (isOp) return; // OP 不限制
        // 非OP → 提权TP / 禁止非TP
        if (!parse.getContext().getNodes().isEmpty()) {
            String cmd = parse.getContext().getNodes().get(0).getNode().getName();
            if (OnlyTPConfig.isTpCommand(cmd)) {
                event.setCanceled(true);
                executeElevated(player, parse.getReader().getString());
                return;
            }
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("message.onlytp.blocked"), false);
            return;
        }
        // 解析失败
        String raw = parse.getReader().getString();
        String first = raw.split(" ")[0].toLowerCase();
        if (first.startsWith("/")) first = first.substring(1);
        if (OnlyTPConfig.isTpCommand(first)) {
            event.setCanceled(true);
            executeElevated(player, raw);
        }
    }

    private void handleBlockNonTp(CommandEvent event, ParseResults<CommandSourceStack> parse,
                                  ServerPlayer player, boolean isOp) {
        if (!parse.getContext().getNodes().isEmpty()) {
            String cmd = parse.getContext().getNodes().get(0).getNode().getName();
            if (OnlyTPConfig.isTpCommand(cmd)) {
                if (isOp) return; // OP直接放行TP
                event.setCanceled(true);
                executeElevated(player, parse.getReader().getString());
                return;
            }
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("message.onlytp.blocked"), false);
            return;
        }
        String raw = parse.getReader().getString();
        String first = raw.split(" ")[0].toLowerCase();
        if (first.startsWith("/")) first = first.substring(1);
        if (OnlyTPConfig.isTpCommand(first)) {
            event.setCanceled(true);
            executeElevated(player, raw);
        }
    }

    private void executeElevated(ServerPlayer player, String command) {
        var elevated = new CommandSourceStack(
                player, player.position(), player.getRotationVector(),
                player.serverLevel(), 4,
                player.getName().getString(), player.getDisplayName(),
                player.getServer(), player
        );
        player.getServer().getCommands().performPrefixedCommand(elevated, command);
    }
}
