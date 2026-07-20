package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
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
 * 游戏模式切换器拦截 Mixin — 拦截 ServerGamePacketListenerImpl#handleChangeGameMode
 * F3+F4 游戏模式切换器使用独立数据包，不经过指令系统
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class GameModeMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "handleChangeGameMode", at = @At("HEAD"), cancellable = true)
    private void onHandleChangeGameMode(ServerboundChangeGameModePacket packet, CallbackInfo ci) {
        if (player == null) return;

        boolean mode2or3 = OnlyTPConfig.isBlockNonTpMode() || OnlyTPConfig.isBothMode();

        if (!mode2or3) return;

        boolean isOp = player.permissions() instanceof LevelBasedPermissionSet lbs
                && lbs.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);

        if (!isOp) return;

        // 黑名单豁免
        if (OnlyTPConfig.isBlacklisted(player.getName().getString())) return;

        player.sendSystemMessage(Component.translatable("message.onlytp.blocked_gamemode"));
        ci.cancel();
    }
}
