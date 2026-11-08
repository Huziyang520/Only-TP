package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.ClientPermissionLogic;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

    private static final int SELECTOR_PERMISSION_LEVEL = 2;

    @Inject(method = "hasPermission", at = @At("HEAD"), cancellable = true, require = 1)
    private void onlytp$grantEntitySelectors(int level, CallbackInfoReturnable<Boolean> cir) {
        if (level == SELECTOR_PERMISSION_LEVEL && ClientPermissionLogic.shouldGrantEntitySelectors()) {
            cir.setReturnValue(true);
        }
    }
}
