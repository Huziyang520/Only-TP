package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.GameModeLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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