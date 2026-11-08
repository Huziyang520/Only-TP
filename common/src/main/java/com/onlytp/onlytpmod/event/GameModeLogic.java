package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class GameModeLogic {

    public static boolean shouldBlock(ServerPlayer player, GameType oldGameMode) {

        if (OnlyTPConfig.isDisabled() || OnlyTPConfig.isAllowTpOnlyMode()) return false;

        if (!player.hasPermissions(2)) return false;

        String playerName = player.getName().getString();
        if (OnlyTPConfig.isBlacklisted(playerName)) return false;

        if (OnlyTPConfig.isWhitelistedForOp("gamemode")) return false;

        player.sendSystemMessage(ModMsg.red(player, "message.onlytp.blocked_gamemode"), true);
        return true;
    }
}