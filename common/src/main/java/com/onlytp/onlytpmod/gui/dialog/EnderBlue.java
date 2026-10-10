package com.onlytp.onlytpmod.gui.dialog;

import com.avalon.base.gui.theme.ModernTheme;
import com.avalon.base.gui.theme.Palette;

/**
 * Local "Ender Blue" palette and theme used only by OnlyTP's standalone
 * dialogs. Built entirely from AvalonBase's public theme API
 * ({@link Palette#builder()} + {@link ModernTheme}); no AvalonBase class is
 * modified. The one-pixel button frame is added at the screen layer by
 * {@link com.onlytp.onlytpmod.gui.ButtonFrames}, because wrapping ModernTheme
 * (which is final) would make ThemedButton fall back to the zero-alpha
 * text color and render transparent labels.
 */
final class EnderBlue {

    static final Palette PALETTE = Palette.builder()
            .panelTop(0xFF121B30)
            .panelBottom(0xFF0A1120)
            .panelShadow(0x44000000)
            .border(0xFF4E74A8)
            .cardBorder(0xFF2E4670)
            .cardFill(0xFF101A2C)
            .btnPrimary(0xFF2F6AAE)
            .btnPrimaryHover(0xFF4F8FD4)
            .btnNeutral(0xFF23304A)
            .btnNeutralHover(0xFF3B4C6E)
            .btnDisabled(0xFF1A2336)
            .btnText(0xFFFFFFFF)
            .toggleBox(0xFF23304A)
            .toggleFill(0xFF62BEF5)
            .toggleBorder(0xFF4E74A8)
            .scrollTrack(0xFF101A2C)
            .scrollThumb(0xFF4E74A8)
            .scrollThumbHighlight(0xFF7299CE)
            .accent(0xFF62BEF5)
            .title(0xFFEAF4FF)
            .label(0xFF87C9F7)
            .text(0xFFC8D6EA)
            .disabled(0xFF5E6E8C)
            .ok(0xFF7FE0A0)
            .warn(0xFFE05555)
            .build();

    /** Buttons use this real ModernTheme instance so their labels resolve btnText correctly. */
    static final ModernTheme THEME = new ModernTheme(PALETTE);

    private EnderBlue() {
    }
}
