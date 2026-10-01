package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/**
 * 游戏模式切换拦截业务逻辑（从原 Forge GameModeHandler 拆分，去除 Forge 事件 API）。
 */
public class GameModeLogic {

    /**
     * 判断是否应拦截本次游戏模式切换。
     *
     * @param player   切换游戏模式的玩家
     * @param oldGameMode 当前（切换前）的游戏模式
     * @return true 表示应拦截（回滚到旧模式）
     */
    public static boolean shouldBlock(ServerPlayer player, GameType oldGameMode) {
        // 仅在 block_non_tp 或 both 模式拦截
        if (OnlyTPConfig.isDisabled() || OnlyTPConfig.isAllowTpOnlyMode()) return false;

        // 非 OP 不拦截
        if (!player.hasPermissions(2)) return false;

        // 黑名单豁免
        String playerName = player.getName().getString();
        if (OnlyTPConfig.isBlacklisted(playerName)) return false;

        // OP 命令白名单包含 gamemode 时放行游戏模式切换（与命令白名单语义一致）
        if (OnlyTPConfig.isWhitelistedForOp("gamemode")) return false;

        // 提示并回滚
        player.displayClientMessage(Component.translatable("message.onlytp.blocked_gamemode"), true);
        return true;
    }

    /**
     * Forge 侧入口：切换游戏模式事件触发。若应拦截则回滚到旧模式。
     */
    public static void onChangeGameMode(ServerPlayer player, GameType oldGameMode) {
        if (shouldBlock(player, oldGameMode)) {
            player.setGameMode(oldGameMode);
        }
    }
}
