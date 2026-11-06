package com.onlytp.onlytpmod.mixin;

import com.onlytp.onlytpmod.event.ClientPermissionLogic;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端：把"实体选择器"权限并入本地命令解析/补全所用的权限集。
 *
 * <p>本地命令解析与补全用的是 {@code ClientSuggestionProvider#permissions()}：非OP 不含
 * {@code commands/entity_selectors}，于是 {@code /tp @e[...] @s} 会在本地报"不能使用选择器"、
 * 选择器参数不补全（服务端因提权执行仍会生效）。此处按
 * {@link ClientPermissionLogic#shouldGrantEntitySelectors()} 的判定把该权限补上，
 * 使本地行为与服务端的提权口径一致。
 *
 * <p>只在客户端生效（本类挂在 mixin 配置的 {@code client} 列表里）。
 */
@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

    @Inject(method = "permissions", at = @At("RETURN"), cancellable = true)
    private void onlytp$grantEntitySelectors(CallbackInfoReturnable<PermissionSet> cir) {
        cir.setReturnValue(ClientPermissionLogic.grant(cir.getReturnValue()));
    }
}
