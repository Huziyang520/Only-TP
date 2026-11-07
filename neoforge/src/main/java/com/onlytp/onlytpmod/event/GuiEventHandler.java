package com.onlytp.onlytpmod.event;

import com.camelot.ui.ConfigScreens;
import com.camelot.ui.Screens;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 客户端：暂停界面按钮，打开 NeoForge 自带的配置界面。
 */
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
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (!OnlyTPConfig.enableConfigEntry()) {
            return;
        }

        int btnSize = 20;
        int x = event.getScreen().width - btnSize - 5;

        event.addListener(new Button(x, 5, btnSize, btnSize, Component.empty(),
                btn -> {
                    var screen = ConfigScreens.create(Constants.MOD_ID, null);
                    if (screen != null) {
                        Screens.open(screen);
                    }
                },
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
