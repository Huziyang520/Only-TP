package com.onlytp.onlytpmod.gui;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

/**
 * Single entry point used by every mod-list config button (Mod Menu, Catalogue,
 * NeoForge mod list) to route into the OnlyTP visual editor.
 *
 * <p><b>Bytecode rule:</b> this class must never statically reference an
 * AvalonBase type (dialogs) or {@link OnlyTPScreen} (which references AvalonBase
 * types). Everything is loaded reflectively after the
 * {@link AvalonLink#isAvalonLoaded()} guard, so the mod list stays healthy when
 * AvalonBase is absent (the factory then returns {@code null}).</p>
 */
public final class ConfigScreenEntry {

    private static final String EDITOR_CLASS = "com.onlytp.onlytpmod.gui.OnlyTPScreen";
    private static final String ACCESS_DENIED_CLASS = "com.onlytp.onlytpmod.gui.dialog.AccessDeniedDialog";
    private static final String LOCAL_NOTICE_CLASS = "com.onlytp.onlytpmod.gui.dialog.LocalConfigNoticeDialog";

    private ConfigScreenEntry() {
    }

    /**
     * Creates the screen the mod list should show, or {@code null} when
     * AvalonBase is not installed.
     */
    public static Screen create(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        if (!AvalonLink.isAvalonLoaded()) {
            return null;
        }
        try {
            if (mc.player != null) {
                // 世界内：入口只由「是否拥有编辑权限（OP）」决定，不再由 show_pause_button 决定。
                // 否则把 show_pause_button 关掉后，世界内既没有暂停页按钮、模组列表入口也会被拒，
                // 形成无法回退的单向闩锁（开关由 false 改回 true 时游戏内无法再打开编辑器）。
                // show_pause_button 回归其字面含义：只控制暂停页那一个按钮的显示。
                boolean canEdit = mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
                if (!canEdit) {
                    return newAccessDenied(parent);
                }
                return newEditor(parent, false);
            }
            // Main menu: reload the local file first so static fields cannot keep
            // values left over from the last server session.
            OnlyTPConfig.load();
            if (OnlyTPConfig.showLocalConfigNotice == 0) {
                return newEditor(parent, true);
            }
            return newLocalNotice(parent);
        } catch (Exception e) {
            Constants.LOG.error("[OnlyTP] Failed to create config screen entry", e);
            return null;
        }
    }

    private static Screen newEditor(Screen parent, boolean localEdit) throws Exception {
        Class<?> clazz = Class.forName(EDITOR_CLASS);
        Constructor<?> ctor = clazz.getConstructor(Screen.class, boolean.class);
        return (Screen) ctor.newInstance(parent, localEdit);
    }

    private static Screen newAccessDenied(Screen parent) throws Exception {
        Class<?> clazz = Class.forName(ACCESS_DENIED_CLASS);
        Constructor<?> ctor = clazz.getConstructor(Screen.class, Component.class, Component.class, Component.class);
        return (Screen) ctor.newInstance(parent,
                Component.translatable("gui.onlytp.notice_title"),
                Component.translatable("gui.onlytp.entry_closed"),
                Component.translatable("gui.onlytp.cancel"));
    }

    private static Screen newLocalNotice(Screen parent) throws Exception {
        Class<?> clazz = Class.forName(LOCAL_NOTICE_CLASS);
        Constructor<?> ctor = clazz.getConstructor(Screen.class, Component.class, Component.class,
                Component.class, Component.class, Component.class, Consumer.class);
        Consumer<Boolean> onConfirm = checked -> {
            try {
                // The checkbox choice is persisted ONLY when the player confirms.
                if (checked) {
                    OnlyTPConfig.showLocalConfigNotice = 0;
                    OnlyTPConfig.save();
                }
                Minecraft.getInstance().setScreenAndShow(newEditor(parent, true));
            } catch (Exception e) {
                Constants.LOG.error("[OnlyTP] Failed to open local editor", e);
            }
        };
        return (Screen) ctor.newInstance(parent,
                Component.translatable("gui.onlytp.notice_title"),
                Component.translatable("gui.onlytp.local_config_notice"),
                Component.translatable("gui.onlytp.dont_show_again"),
                Component.translatable("gui.onlytp.cancel"),
                Component.translatable("gui.onlytp.confirm"),
                onConfirm);
    }
}
