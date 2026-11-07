package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.ClientPermissionLogic;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client: merges the selector permission into the set used for local parsing and suggestions.
 * <p>Local parsing uses {@code ClientSuggestionProvider#permissions()}, which lacks
 * {@code commands/entity_selectors}, so {@code /tp @e[...] @s} fails locally
 */
@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

    @Inject(method = "permissions", at = @At("RETURN"), cancellable = true)
    private void onlytp$grantEntitySelectors(CallbackInfoReturnable<PermissionSet> cir) {
        cir.setReturnValue(ClientPermissionLogic.grant(cir.getReturnValue()));
    }
}
