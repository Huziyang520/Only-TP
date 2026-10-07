package com.onlytp.onlytpmod.gui;

import com.avalon.base.gui.theme.Palette;
import com.avalon.base.gui.theme.ThemedButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/**
 * Screen-layer one-pixel right-angle frames for {@link ThemedButton}s.
 *
 * <p>The frame cannot be added by wrapping {@code GuiTheme}: AvalonBase's
 * ModernTheme is final and ThemedButton only reads btnText from an actual
 * ModernTheme instance, so a wrapper makes the fallback text color 0xFFFFFF
 * (zero alpha) and button labels render fully transparent. Keeping the real
 * ModernTheme as the button theme and outlining buttons here after the
 * widgets have rendered avoids touching AvalonBase.</p>
 */
public final class ButtonFrames {

    private ButtonFrames() {
    }

    /**
     * Outline every themed button on the screen, active or not.
     *
     * <p>Inactive buttons used to be skipped, which made a disabled button (for example the
     * "Add" button while the input box is empty) look like it had no border at all. The
     * border is now always drawn; only an active, hovered button is highlighted with the
     * accent colour, so a disabled button stays visually "flat" but still framed.
     */
    public static void render(GuiGraphicsExtractor g, Screen screen, Palette palette) {
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
