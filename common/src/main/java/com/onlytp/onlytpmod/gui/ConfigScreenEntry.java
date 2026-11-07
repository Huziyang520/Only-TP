package com.onlytp.onlytpmod.gui;

import com.camelot.ui.ConfigScreens;
import com.camelot.ui.Screens;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;

/**
 * Config button entry of the mod list.
 * <p>The platform decides the screen: NeoForge's own ConfigurationScreen, or a Cloth screen on Fabric.
 * Returns {@code null} when neither exists, so the button is disabled rather than falling back anywhere.
 */
public final class ConfigScreenEntry {

    private static final String AVALON_SCREEN = "com.onlytp.onlytpmod.gui.OnlyTPScreen";

    private ConfigScreenEntry() {
    }

    /** Mod list entry: a closed notice inside a world, otherwise the platform screen. */
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

    /** Pause button: the only entry to the AvalonBase screen; does nothing without AvalonBase. */
    public static void openAvalonScreen() {
        Screen screen = avalonScreen(null);
        if (screen != null) {
            Screens.open(screen);
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
