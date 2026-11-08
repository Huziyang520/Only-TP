package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import com.onlytp.onlytpmod.gui.PauseScreenButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class GuiEventHandler {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(GuiEventHandler::onScreenInit);
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }
        if (!AvalonLink.isAvalonLoaded()) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!OnlyTPConfig.enableConfigEntry()) {
            return;
        }

        int btnSize = 20;
        int x = event.getScreen().width - btnSize - 5;

        event.addListener(PauseScreenButton.create(x, 5, btnSize,
                () -> ConfigScreenEntry.openAvalonScreen()));
    }
}
