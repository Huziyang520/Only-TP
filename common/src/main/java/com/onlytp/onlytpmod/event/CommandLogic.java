package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class CommandLogic {

    public static boolean shouldCancel(ParseResults<CommandSourceStack> parseResults) {
        if (OnlyTPConfig.isDisabled()) return false;
        CommandSourceStack source = parseResults.getContext().getSource();
        String raw = parseResults.getReader().getString();
        String cmd = resolveCommandName(parseResults, raw);
        return handle(source, raw, cmd);
    }

    public static boolean shouldCancel(CommandSourceStack source, String raw) {
        if (OnlyTPConfig.isDisabled()) return false;
        String cmd = resolveCommandNameFromRaw(raw);
        return handle(source, raw, cmd);
    }

    private static String resolveCommandName(ParseResults<CommandSourceStack> parse, String raw) {
        if (!parse.getContext().getNodes().isEmpty()) {
            return parse.getContext().getNodes().get(0).getNode().getName();
        }
        return resolveCommandNameFromRaw(raw);
    }

    private static String resolveCommandNameFromRaw(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String first = raw.split(" ")[0].toLowerCase();
        if (first.startsWith("/")) first = first.substring(1);
        return first;
    }

    private static boolean handle(CommandSourceStack source, String raw, String cmd) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return false;

        String playerName = player.getName().getString();
        boolean isOp = source.hasPermission(2);

        if (OnlyTPConfig.isBothMode()) {

            boolean canceled = false;
            if (!OnlyTPConfig.blacklistAllowTp().contains(playerName)) {
                canceled |= handleAllowTpOnly(player, raw, cmd, isOp);
            }
            if (!OnlyTPConfig.blacklistBlockNonTp().contains(playerName)) {
                canceled |= handleBlockNonTp(player, raw, cmd, isOp);
            }
            return canceled;
        } else {

            if (OnlyTPConfig.isBlacklisted(playerName)) return false;

            if (OnlyTPConfig.isAllowTpOnlyMode()) {
                return handleAllowTpOnly(player, raw, cmd, isOp);
            } else if (OnlyTPConfig.isBlockNonTpMode()) {
                return handleBlockNonTp(player, raw, cmd, isOp);
            }
        }
        return false;
    }

    private static boolean handleAllowTpOnly(ServerPlayer player, String raw, String cmd, boolean isOp) {
        if (isOp) return false;

        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }
        return false;
    }

    private static boolean deniesEntitySelector(String raw, ServerPlayer player) {
        if (OnlyTPConfig.allowEntitySelectors()) return false;
        if (raw == null || raw.indexOf('@') < 0) return false;
        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.selector_blocked"), false);
        return true;
    }

    private static boolean handleBlockNonTp(ServerPlayer player, String raw, String cmd, boolean isOp) {
        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (isOp) return false;
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }

        if (isOp && OnlyTPConfig.isWhitelistedForOp(cmd)) {
            return false;
        }

        if ("gamemode".equals(cmd) || "g".equals(cmd)) {
            player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked_gamemode"), false);
            return true;
        }
        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked"), false);
        return true;
    }

    private static void executeElevated(ServerPlayer player, String command) {
        var elevated = player.createCommandSourceStack().withPermission(4);
        player.level().getServer().getCommands().performPrefixedCommand(elevated, command);
    }
}