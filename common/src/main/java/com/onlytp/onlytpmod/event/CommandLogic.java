package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

/**
 * <p>Two equivalent entry points:
 * both share one decision path.
 */
public class CommandLogic {

    /**
     * /**
     * Forge entry: results are already parsed.
     */
    public static boolean shouldCancel(ParseResults<CommandSourceStack> parseResults) {
        if (OnlyTPConfig.isDisabled()) return false;
        CommandSourceStack source = parseResults.getContext().getSource();
        String raw = parseResults.getReader().getString();
        String cmd = resolveCommandName(parseResults, raw);
        return handle(source, raw, cmd);
    }

    /**
     * /**
     * Fabric mixin entry: only a source and a command string, so the name is parsed here.
     */
    public static boolean shouldCancel(CommandSourceStack source, String raw) {
        if (OnlyTPConfig.isDisabled()) return false;
        String cmd = resolveCommandNameFromRaw(raw);
        return handle(source, raw, cmd);
    }

    /**
     * /**
     * Extracts the command name from ParseResults (parsed node first, raw string as fallback).
     */
    private static String resolveCommandName(ParseResults<CommandSourceStack> parse, String raw) {
        if (!parse.getContext().getNodes().isEmpty()) {
            return parse.getContext().getNodes().get(0).getNode().getName();
        }
        return resolveCommandNameFromRaw(raw);
    }

    /**
     * /**
     * Extracts the first word of a raw command string, dropping a leading slash.
     */
    private static String resolveCommandNameFromRaw(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String first = raw.split(" ")[0].toLowerCase();
        if (first.startsWith("/")) first = first.substring(1);
        return first;
    }

    private static boolean handle(CommandSourceStack source, String raw, String cmd) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return false;

        String playerName = player.getName().getString();
        boolean isOp = source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

        if (OnlyTPConfig.isBothMode()) {
            // Both modes enabled: each blacklist applies separately
            boolean canceled = false;
            if (!OnlyTPConfig.blacklistAllowTp().contains(playerName)) {
                canceled |= handleAllowTpOnly(player, raw, cmd, isOp);
            }
            if (!OnlyTPConfig.blacklistBlockNonTp().contains(playerName)) {
                canceled |= handleBlockNonTp(player, raw, cmd, isOp);
            }
            return canceled;
        } else {
            // Blacklist: fully exempt
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
        if (isOp) return false; // OP 不限制
        // Non-operator: teleport commands are elevated, everything else stays vanilla
        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }
        return false;
    }

    /**
     * /**
     * Blocks elevated commands that use a target selector while the selector switch is off.
     */
    private static boolean deniesEntitySelector(String raw, ServerPlayer player) {
        if (OnlyTPConfig.allowEntitySelectors()) return false;
        if (raw == null || raw.indexOf('@') < 0) return false;
        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.selector_blocked"), false);
        return true;
    }

    private static boolean handleBlockNonTp(ServerPlayer player, String raw, String cmd, boolean isOp) {
        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (isOp) return false; // OP直接放行TP
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }
        // Operator hit by the command whitelist is unblocked; other commands stay blocked
        if (isOp && OnlyTPConfig.isWhitelistedForOp(cmd)) {
            return false;
        }
        // A blocked gamemode command answers with the gamemode message instead of the generic one
        if ("gamemode".equals(cmd) || "g".equals(cmd)) {
            player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked_gamemode"), false);
            return true;
        }
        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked"), false);
        return true;
    }

    private static void executeElevated(ServerPlayer player, String command) {
        var elevated = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
        player.level().getServer().getCommands().performPrefixedCommand(elevated, command);
    }
}