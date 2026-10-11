package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Catalogue config factory. Catalogue discovers this class by the class name
 * declared in fabric.mod.json ({@code custom.catalogue.configFactory}) and
 * reflectively calls {@link #createConfigScreen(Screen, ModContainer)}; it must
 * never import any Catalogue type. The method may return null (e.g. AvalonBase
 * missing), in which case Catalogue safely does nothing.
 */
public final class CatalogueConfigEntry {

    private CatalogueConfigEntry() {
    }

    public static Screen createConfigScreen(Screen parent, ModContainer container) {
        return ConfigScreenEntry.create(parent);
    }
}
