package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeConfigScreens {

    private NeoForgeConfigScreens() {
    }

    public static void register() {
        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent(container ->
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (modContainer, parent) -> ConfigScreenEntry.create(parent)));
    }
}
