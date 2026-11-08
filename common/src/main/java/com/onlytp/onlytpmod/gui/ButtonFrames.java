package com.onlytp.onlytpmod.gui;

import com.avalon.base.gui.theme.Palette;
import com.avalon.base.gui.theme.ThemedButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class ButtonFrames {

    private ButtonFrames() {
    }

    public static void render(GuiGraphics g, Screen screen, Palette palette) {
        for (var child : screen.children()) {
            if (!(child instanceof ThemedButton btn)) {
                continue;
            }
            int frame = btn.active && btn.isHoveredOrFocused() ? palette.accent : palette.border;
            int x = btn.getX();
            int y = btn.getY();
            int w = btn.getWidth();
            int h = btn.getHeight();
            g.fill(x, y, x + w, y + 1, frame);
            g.fill(x, y + h - 1, x + w, y + h, frame);
            g.fill(x, y, x + 1, y + h, frame);
            g.fill(x + w - 1, y, x + w, y + h, frame);
        }
    }
}
