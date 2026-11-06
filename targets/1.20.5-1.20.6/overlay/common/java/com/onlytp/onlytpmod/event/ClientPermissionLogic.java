package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;

/**
 * 客户端侧的"提权感知"判定：非OP 使用 TP 类指令时的目标选择器。
 *
 * <p>背景：非OP 的 TP 指令由服务端**提权执行**，但**客户端本地**的解析与补全看的是
 * {@code ClientSuggestionProvider}（1.21.8 为 int 权限等级口径），非OP 的
 * {@code allowsRestrictedCommands} 为 false
 * ⇒ 输入 {@code /tp @e[...] @s} 会本地报"不能使用选择器"、选择器参数不补全
 * （服务端仍会执行成功）。
 *
 * <p>本类给出"是否应把该权限视为已授予"的判定，由客户端 mixin
 * {@code com.onlytp.onlytpmod.mixin.ClientSuggestionProviderMixin} 消费；开关
 * {@code allow_entity_selectors} 关闭时判定恒为 false（客户端照原样报错，服务端也会拒绝）。
 */
public final class ClientPermissionLogic {

    private ClientPermissionLogic() {
    }

    /**
     * 是否应在客户端把"实体选择器"权限视为已授予。
     *
     * <p>条件：① 配置开关开启；② 本模式下会给非OP 提权 TP 指令
     * （仅允许TP / 禁止非TP / 同时启用三种模式都算；全关闭模式放行）。
     *
     * <p>1.21.8 落点：本地解析/补全看 {@code ClientSuggestionProvider#allowsSelectors()}
     * （覆写为返回 {@code allowsRestrictedCommands}，非OP 为 false）；判定为 true 时由 mixin
     * 直接把这个返回值置为 true。
     */
    public static boolean shouldGrantEntitySelectors() {
        if (!OnlyTPConfig.allowEntitySelectors) return false;
        if (OnlyTPConfig.isDisabled()) return false;
        return OnlyTPConfig.isAllowTpOnlyMode()
                || OnlyTPConfig.isBlockNonTpMode()
                || OnlyTPConfig.isBothMode();
    }
}
