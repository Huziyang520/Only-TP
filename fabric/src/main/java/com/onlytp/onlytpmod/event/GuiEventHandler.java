package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import com.onlytp.onlytpmod.gui.PauseScreenButton;
import com.onlytp.onlytpmod.mixin.ScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;

public class GuiEventHandler {

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) return;
            if (!AvalonLink.isAvalonLoaded()) return;
            if (Minecraft.getInstance().player == null) return;

            if (Minecraft.getInstance().hasSingleplayerServer()) OnlyTPConfig.CONFIG.reload();
            if (!OnlyTPConfig.enableConfigEntry()) return;

            int btnSize = 20;
            int x = screen.width - btnSize - 5;

            ((ScreenAccessor) screen).onlytp$invokeAddRenderableWidget(
                    PauseScreenButton.create(x, 5, btnSize, () -> ConfigScreenEntry.openAvalonScreen()));
        });
    }
}
