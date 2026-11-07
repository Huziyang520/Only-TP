package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class GuiEventHandler {

    private static final Identifier BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/button.png");

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

        event.addListener(new Button(x, 5, btnSize, btnSize, Component.empty(),
                btn -> ConfigScreenEntry.openAvalonScreen(),
                btn -> Component.empty()
        ) {
            @Override
            public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                extractDefaultSprite(graphics);
                int ox = getX() + (getWidth() - 16) / 2;
                int oy = getY() + (getHeight() - 16) / 2;
                graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, ox, oy, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        });
    }
}
