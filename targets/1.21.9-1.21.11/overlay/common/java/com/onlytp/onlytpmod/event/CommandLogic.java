package com.onlytp.onlytpmod.event;

import com.mojang.brigadier.ParseResults;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.util.ModMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

/**
 * 
 *
 * <p>提供两个等价入口：
 * <ul>
 *   <li>{@link #shouldCancel(ParseResults)} —— Forge 侧（已有 ParseResults）</li>
 *   <li>{@link #shouldCancel(CommandSourceStack, String)} —— Fabric mixin 侧（只有 source 与命令字符串）</li>
 * </ul>
 * 两者共享同一套判定逻辑。
 */
public class CommandLogic {

    /**
     * Forge 侧入口：已有解析结果。
     */
    public static boolean shouldCancel(ParseResults<CommandSourceStack> parseResults) {
        if (OnlyTPConfig.isDisabled()) return false;
        CommandSourceStack source = parseResults.getContext().getSource();
        String raw = parseResults.getReader().getString();
        String cmd = resolveCommandName(parseResults, raw);
        return handle(source, raw, cmd);
    }

    /**
     * Fabric mixin 侧入口：只有命令源与命令字符串，自行解析命令名。
     */
    public static boolean shouldCancel(CommandSourceStack source, String raw) {
        if (OnlyTPConfig.isDisabled()) return false;
        String cmd = resolveCommandNameFromRaw(raw);
        return handle(source, raw, cmd);
    }

    /**
     * 从 ParseResults 提取命令名（优先用已解析节点，解析失败则退回原始字符串）。
     */
    private static String resolveCommandName(ParseResults<CommandSourceStack> parse, String raw) {
        if (!parse.getContext().getNodes().isEmpty()) {
            return parse.getContext().getNodes().get(0).getNode().getName();
        }
        return resolveCommandNameFromRaw(raw);
    }

    /**
     * 从原始命令字符串提取第一个词作为命令名（去掉可选的前导 '/'）。
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
            // 同时启用：黑名单分别应用
            boolean canceled = false;
            if (!OnlyTPConfig.blacklistAllowTp.contains(playerName)) {
                canceled |= handleAllowTpOnly(player, raw, cmd, isOp);
            }
            if (!OnlyTPConfig.blacklistBlockNonTp.contains(playerName)) {
                canceled |= handleBlockNonTp(player, raw, cmd, isOp);
            }
            return canceled;
        } else {
            // 黑名单：完全豁免
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
        // 非OP → 仅提权执行 TP 命令，其余命令保持原版行为（可用则用，不可用则显示"未知或不完整的命令"）
        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }
        return false;
    }

    /**
     * 目标选择器开关关闭时，拒绝**含选择器**的提权指令。
     *
     * <p>只关客户端是不够的：服务端是"用全权限源提权执行"，选择器天然不受限。因此在提权之前
     * 再拦一次，保证"关闭 = 真的不能用"。
     *
     * @return true 表示已拦截（并已提示玩家）
     */
    private static boolean deniesEntitySelector(String raw, ServerPlayer player) {
        if (OnlyTPConfig.allowEntitySelectors) return false;
        if (raw == null || raw.indexOf('@') < 0) return false;
        player.displayClientMessage(ModMsg.red(player, "message.onlytp.selector_blocked"), false);
        return true;
    }

    private static boolean handleBlockNonTp(ServerPlayer player, String raw, String cmd, boolean isOp) {
        if (OnlyTPConfig.isTpCommand(cmd)) {
            if (isOp) return false; // OP直接放行TP
            if (deniesEntitySelector(raw, player)) return true;
            executeElevated(player, raw);
            return true;
        }
        // OP 命中命令白名单 → 放行（可用则用），其余命令仍拦截
        if (isOp && OnlyTPConfig.isWhitelistedForOp(cmd)) {
            return false;
        }
        // gamemode/g 命令被禁时，提示游戏模式切换被禁（而非通用的指令禁止提示）
        if ("gamemode".equals(cmd) || "g".equals(cmd)) {
            player.displayClientMessage(ModMsg.red(player, "message.onlytp.blocked_gamemode"), false);
            return true;
        }
        player.displayClientMessage(ModMsg.red(player, "message.onlytp.blocked"), false);
        return true;
    }

    private static void executeElevated(ServerPlayer player, String command) {
        var elevated = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
        player.level().getServer().getCommands().performPrefixedCommand(elevated, command);
    }
}