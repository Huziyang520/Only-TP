package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu entrypoint: routes the mod list's Config button to the OnlyTP
 * visual editor. Returning null (e.g. AvalonBase missing) makes Mod Menu
 * keep the button disabled.
 */
public class ModMenuConfigEntry implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenEntry::create;
    }
}
