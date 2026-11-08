package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.ClientPermissionLogic;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

    @Inject(method = "allowsSelectors", at = @At("HEAD"), cancellable = true, require = 1)
    private void onlytp$grantEntitySelectors(CallbackInfoReturnable<Boolean> cir) {
        if (ClientPermissionLogic.shouldGrantEntitySelectors()) {
            cir.setReturnValue(true);
        }
    }
}
