package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

/**
 * 客户端侧的"提权感知"判定：非OP 使用 TP 类指令时的目标选择器。
 *
 * <p>背景：非OP 的 TP 指令由服务端**提权执行**，但**客户端本地**的解析与补全看的是
 * {@code ClientSuggestionProvider} 所用权限集，非OP 不含 {@code commands/entity_selectors}
 * ⇒ 输入 {@code /tp @e[...] @s} 会本地报"不能使用选择器"、选择器参数不补全
 * （服务端仍会执行成功）。
 *
 * <p>本类给出"是否应把该权限视为已授予"的判定，由客户端 mixin
 * {@code com.onlytp.onlytpmod.mixin.ClientSuggestionProviderMixin} 消费；开关
 * {@code allow_entity_selectors} 关闭时判定恒为 false（客户端照原样报错，服务端也会拒绝）。
 */
public final class ClientPermissionLogic {

    /** 只含 {@code commands/entity_selectors} 的权限集。 */
    private static final PermissionSet ENTITY_SELECTORS_ONLY = new PermissionSet() {
        @Override
        public boolean hasPermission(Permission permission) {
            return Permissions.COMMANDS_ENTITY_SELECTORS.equals(permission);
        }
    };

    private ClientPermissionLogic() {
    }

    /**
     * 是否应在客户端把"实体选择器"权限视为已授予。
     *
     * <p>条件：① 配置开关开启；② 本模式下会给非OP 提权 TP 指令
     * （仅允许TP / 禁止非TP / 同时启用三种模式都算；全关闭模式放行）。
     */
    public static boolean shouldGrantEntitySelectors() {
        if (!OnlyTPConfig.allowEntitySelectors) return false;
        if (OnlyTPConfig.isDisabled()) return false;
        return OnlyTPConfig.isAllowTpOnlyMode()
                || OnlyTPConfig.isBlockNonTpMode()
                || OnlyTPConfig.isBothMode();
    }

    /** 按判定结果把"实体选择器"权限并入原权限集（客户端 mixin 调用）。 */
    public static PermissionSet grant(PermissionSet original) {
        if (original == null || !shouldGrantEntitySelectors()) return original;
        return original.union(ENTITY_SELECTORS_ONLY);
    }
}
