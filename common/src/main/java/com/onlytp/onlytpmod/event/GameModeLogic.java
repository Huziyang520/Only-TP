package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.GameType;

/**
 * Game mode switching rules (split from the old Forge handler, no Forge event API).
 */
public class GameModeLogic {

    /**
     * /**
     * Whether this game mode change should be blocked.
     */
    public static boolean shouldBlock(ServerPlayer player, GameType oldGameMode) {
        // Blocks only in block_non_tp or both modes
        if (OnlyTPConfig.isDisabled() || OnlyTPConfig.isAllowTpOnlyMode()) return false;

        // Never blocks non-operators
        if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return false;

        // Blacklist exemption
        String playerName = player.getName().getString();
        if (OnlyTPConfig.isBlacklisted(playerName)) return false;

        // A whitelisted gamemode command unblocks the switcher, matching command whitelist semantics
        if (OnlyTPConfig.isWhitelistedForOp("gamemode")) return false;

        // Notify and roll back
        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked_gamemode"), true);
        return true;
    }
}