package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.GameModeLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 游戏模式切换拦截（等价于原 Forge PlayerChangeGameModeEvent）。
 * 在 setGameMode 时捕获旧模式，若应拦截则取消本次切换（模式保持旧值，等价于原版回滚）。
 *
 * <p>注意：setGameMode 存在多个重载（ServerPlayer 的返回 boolean，Player 的返回 void），
 * 因此必须用完整描述符指定目标方法，避免 mixin 因签名歧义而失败。
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "setGameMode(Lnet/minecraft/world/level/GameType;)Z", at = @At("HEAD"), cancellable = true)
    private void onlytp$interceptGameMode(GameType newGameMode, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        GameType oldGameMode = self.gameMode.getGameModeForPlayer();
        if (GameModeLogic.shouldBlock(self, oldGameMode)) {
            cir.setReturnValue(false);
        }
    }
}
