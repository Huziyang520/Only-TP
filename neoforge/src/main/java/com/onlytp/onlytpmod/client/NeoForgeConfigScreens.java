package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ConfigScreenHandler;

/**
 * Registers the native NeoForge config-screen extension point so the vanilla
 * mod list (as well as Catalogue and Configured, which both query the same
 * extension point) shows a Config button routing to the OnlyTP visual editor.
 *
 * <p>When AvalonBase is absent this registers nothing: the button is hidden
 * rather than broken, because the editor cannot be loaded.</p>
 */
public final class NeoForgeConfigScreens {

    private NeoForgeConfigScreens() {
    }

    public static void register() {
        if (!AvalonLink.isAvalonLoaded()) {
            return;
        }
        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent(container -> {
            // 1.20.2-1.20.4（NeoForge 20.4.x）的扩展点是 ConfigScreenHandler.ConfigScreenFactory；
            // 1.21 起才改名成 client.gui.IConfigScreenFactory。
            // 20.4.x 的 registerExtensionPoint 只收 (Class, Supplier)（1.21 起才收实例），故传 lambda。
            container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> ConfigScreenEntry.create(parent)));
        });
    }
}
