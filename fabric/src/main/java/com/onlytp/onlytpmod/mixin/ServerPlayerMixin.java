package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.GameModeLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric game mode blocking (equivalent to Forge's PlayerChangeGameModeEvent).
 * Captures the old mode in setGameMode and cancels the change, leaving the old value in place.
 * <p>Note: setGameMode has several overloads (ServerPlayer returns boolean, Player returns void),
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