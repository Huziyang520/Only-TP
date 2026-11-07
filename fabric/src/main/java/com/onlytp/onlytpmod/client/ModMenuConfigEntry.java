package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuConfigEntry implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenEntry::create;
    }
}
