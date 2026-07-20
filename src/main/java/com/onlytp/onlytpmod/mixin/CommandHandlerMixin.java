package com.onlytp.onlytpmod.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 命令拦截 Mixin — 拦截 ServerGamePacketListenerImpl#handleChatCommand
  * 所有玩家聊天指令均由此方法进入
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class CommandHandlerMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
    private void onHandleChatCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (OnlyTPConfig.isDisabled()) return;
        if (player == null) return;

        String raw = packet.command(); // 不含 "/" 前缀
        String playerName = player.getName().getString();

        boolean isOp = player.permissions() instanceof LevelBasedPermissionSet lbs
                && lbs.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);

        String first = raw.split(" ")[0].toLowerCase();
        boolean isTp = OnlyTPConfig.isTpCommand(first);

        // ── 三种模式处理 ──
        if (OnlyTPConfig.isBothMode()) {
            if (!OnlyTPConfig.blacklistAllowTp.contains(playerName)) {
                if (handleAllowTpOnly(player, isOp, raw, isTp)) { ci.cancel(); return; }
            }
            if (!OnlyTPConfig.blacklistBlockNonTp.contains(playerName)) {
                if (handleBlockNonTp(player, isOp, raw, isTp)) { ci.cancel(); }
            }
        } else {
            if (OnlyTPConfig.isBlacklisted(playerName)) return;

            if (OnlyTPConfig.isAllowTpOnlyMode()) {
                if (handleAllowTpOnly(player, isOp, raw, isTp)) ci.cancel();
            } else if (OnlyTPConfig.isBlockNonTpMode()) {
                if (handleBlockNonTp(player, isOp, raw, isTp)) ci.cancel();
            }
        }
    }

    /**
     * @return true 表示需要取消原指令执行
     */
    private static boolean handleAllowTpOnly(ServerPlayer player, boolean isOp,
                                              String command, boolean isTp) {
        if (isOp) return false; // OP 放行
        if (isTp) {
            executeElevated(player, command);
            return true; // 取消原指令，用提权后的指令代替
        }
        player.sendSystemMessage(Component.translatable("message.onlytp.blocked"));
        return true; // 阻挡
    }

    /**
     * @return true 表示需要取消原指令执行
     */
    private static boolean handleBlockNonTp(ServerPlayer player, boolean isOp,
                                             String command, boolean isTp) {
        // mode=block_non_tp: 仅阻挡 OP 的非TP指令
        if (isTp) return false;
        if (isOp) {
            player.sendSystemMessage(Component.translatable("message.onlytp.blocked"));
            return true;
        }
        return false;
    }

    private static void executeElevated(ServerPlayer player, String command) {
        try {
            var elevated = new CommandSourceStack(
                    player.level().getServer(),
                    player.position(),
                    player.getRotationVector(),
                    player.level(),
                    LevelBasedPermissionSet.OWNER,
                    player.getName().getString(),
                    player.getDisplayName(),
                    player.level().getServer(),
                    player
            );
            // command 不含 "/"，dispatcher.execute 直接接受
            player.level().getServer().getCommands().getDispatcher().execute(command, elevated);
        } catch (CommandSyntaxException e) {
            player.sendSystemMessage(Component.literal(e.getMessage()));
        }
    }
}
