package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.OnlyTPScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

public class GuiEventHandler {

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (!OnlyTPConfig.showPauseButton) return;

            var btn = Button.builder(
                    Component.literal("TP"),
                    btn2 -> mc.setScreenAndShow(new OnlyTPScreen())
                ).bounds(screen.width - 25, 5, 20, 20)
                .build();

            Screens.getWidgets(screen).add(btn);
        });
    }
}
