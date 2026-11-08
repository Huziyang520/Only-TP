package com.onlytp.onlytpmod.gui;

import com.onlytp.onlytpmod.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class PauseScreenButton {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/button.png");

    private PauseScreenButton() {
    }

    public static Button create(int x, int y, int size, Runnable onClick) {
        return new Button(x, y, size, size, Component.empty(),
                b -> onClick.run(), b -> Component.empty()) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                int ox = getX() + (getWidth() - 16) / 2;
                int oy = getY() + (getHeight() - 16) / 2;
                graphics.blit(TEXTURE, ox, oy, 0, 0, 16, 16, 16, 16);
            }
        };
    }
}
