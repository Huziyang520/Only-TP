package com.onlytp.onlytpmod;

/**
 * AvalonBase 联动探测门面。
 *
 * <p>OnlyTP 的核心功能（命令限制 / 游戏模式限制 / 配置）可脱离 AvalonBase 独立运行，
 * 仅当同时安装 AvalonBase 时才启用「暂停页面按钮 + 编辑界面 + 网络配置同步」等联动增强。
 *
 * <p>此处用纯 JDK 反射判断 AvalonBase 是否加载（以 {@code com.avalon.base.Constants} 作为标志类），
 * 不依赖任何平台 API，也无需修改 AvalonBase 自身代码，在 Forge / Fabric 两端通用。
 */
public final class AvalonLink {

    /** AvalonBase 的标志类（用于判断其是否已加载）。 */
    private static final String MARKER = "com.avalon.base.Constants";

    /** 结果缓存：AvalonBase 是否已加载。 */
    private static final boolean AVALON_PRESENT = isPresent();

    private AvalonLink() {
    }

    private static boolean isPresent() {
        try {
            Class.forName(MARKER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @return {@code true} 表示 AvalonBase 已安装，可启用联动增强（GUI 按钮 / 网络同步）。
     */
    public static boolean isAvalonLoaded() {
        return AVALON_PRESENT;
    }
}
