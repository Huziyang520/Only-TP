package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

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
            // Explicit local type: a bare lambda is ambiguous between the
            // registerExtensionPoint(Class, T) and (Class, Supplier<T>) overloads.
            IConfigScreenFactory factory = (modContainer, parent) -> ConfigScreenEntry.create(parent);
            container.registerExtensionPoint(IConfigScreenFactory.class, factory);
        });
    }
}
