package com.onlytp.onlytpmod.gui;

import com.camelot.ui.ConfigScreens;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;

/**
 * 模组列表的「配置」按钮入口。
 *
 * <p>具体界面由平台决定：NeoForge 用自带的 ConfigurationScreen，Fabric 用 Cloth 生成的界面。
 * 两者都没有时返回 {@code null}，按钮置灰——不会退到暂停界面按钮所用的 AvalonBase 界面。
 *
 * <p><b>字节码约束</b>：本类不得静态引用 {@code OnlyTPScreen}（它引用 AvalonBase 类型），
 * 一律在 {@link AvalonLink#isAvalonLoaded()} 守卫后反射创建。
 */
public final class ConfigScreenEntry {

    private static final String AVALON_SCREEN = "com.onlytp.onlytpmod.gui.OnlyTPScreen";

    private ConfigScreenEntry() {
    }

    /** 模组列表入口：世界内且服务端关闭入口时给二级提示，否则交给平台的配置界面。 */
    public static Screen create(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !OnlyTPConfig.enableConfigEntry()) {
            return ConfigScreens.entryClosed(parent,
                    Component.translatable("gui.onlytp.title"),
                    Component.translatable("gui.onlytp.entry_closed"),
                    Component.translatable("gui.done"));
        }
        return ConfigScreens.create(Constants.MOD_ID, parent);
    }

    /** 暂停界面按钮：AvalonBase 界面的唯一入口。未装 AvalonBase 时什么也不做。 */
    public static void openAvalonScreen() {
        Screen screen = avalonScreen(null);
        if (screen != null) {
            Minecraft.getInstance().setScreenAndShow(screen);
        }
    }

    private static Screen avalonScreen(Screen parent) {
        if (!AvalonLink.isAvalonLoaded()) {
            return null;
        }
        try {
            Class<?> type = Class.forName(AVALON_SCREEN);
            Constructor<?> ctor = type.getConstructor(Screen.class);
            return (Screen) ctor.newInstance(parent);
        } catch (Exception e) {
            Constants.LOG.error("[OnlyTP] Failed to open the AvalonBase config screen", e);
            return null;
        }
    }
}
