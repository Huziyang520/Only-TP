package com.onlytp.onlytpmod;

/**
 * AvalonBase 探测门面。
 * <p>核心功能（命令限制 / 游戏模式限制 / 配置）可脱离 AvalonBase 独立运行；装了它才多出
 * 「暂停界面按钮 + 它的可视化配置界面」这条入口。用纯 JDK 反射判断，不引用任何平台 API。
 */
public final class AvalonLink {

    /** AvalonBase 的标志类。 */
    private static final String MARKER = "com.avalon.base.Constants";

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

    public static boolean isAvalonLoaded() {
        return AVALON_PRESENT;
    }
}
