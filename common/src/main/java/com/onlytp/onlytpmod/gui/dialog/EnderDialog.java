package com.onlytp.onlytpmod.gui.dialog;

import com.onlytp.onlytpmod.gui.ButtonFrames;
import com.avalon.base.gui.theme.GuiTheme;
import com.avalon.base.gui.theme.ModernTheme;
import com.avalon.base.gui.theme.ThemedButton;
import com.avalon.base.gui.util.TextFit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Base for OnlyTP's standalone modal dialogs.
 *
 * <p>Visual style mirrors the AvalonBase editor screens (dark backdrop,
 * gradient right-angle panel, centered title, wrapped body text) but the
 * palette is the local Ender Blue one. Buttons are regular
 * {@link ThemedButton}s rendered by a real ModernTheme instance, and the
 * one-pixel right-angle frame is drawn on top by {@link ButtonFrames}
 * after the widgets render.</p>
 *
 * <p>Subclasses only declare the bottom area height, register their buttons
 * via {@link #addDialogButton} and optionally draw extra content via
 * {@link #renderDialogContents}.</p>
 */
abstract class EnderDialog extends Screen {

    protected static final int PANEL_W = 260;
    protected static final int BTN_H = 20;
    protected static final int SIDE = 12;
    protected static final int ROW_GAP = 8;
    protected static final int BOTTOM_PAD = 12;

    private static final int TITLE_Y = 9;
    private static final int TEXT_Y = 27;
    private static final int LINE_H = 11;
    private static final int BACKDROP_COLOR = 0xC0101010;

    protected final Screen parentScreen;
    protected final GuiTheme theme;
    protected int left;
    protected int top;
    protected int panelW;
    protected int panelH;
    protected List<String> lines = List.of();

    private final Component bodyText;

    protected EnderDialog(Screen parent, Component title, Component body, GuiTheme theme) {
        super(title);
        this.parentScreen = parent;
        this.bodyText = body;
        this.theme = theme;
    }

    /** Minimum panel height regardless of the wrapped text length. */
    protected abstract int minPanelH();

    /** Height of everything below the wrapped body text (gaps, rows, buttons, padding). */
    protected abstract int bottomHeight();

    /** Register buttons after the panel geometry has been computed. */
    protected abstract void addDialogWidgets();

    @Override
    protected void init() {
        this.lines = TextFit.wrap(font, bodyText.getString(), PANEL_W - 2 * SIDE);
        this.panelW = PANEL_W;
        this.panelH = Math.max(minPanelH(), TEXT_Y + lines.size() * LINE_H + bottomHeight());
        this.left = (width - panelW) / 2;
        this.top = Math.max(4, (height - panelH) / 2);
        addDialogWidgets();
    }

    /** Absolute Y of the single button row at the bottom of the panel. */
    protected int buttonsY() {
        return top + panelH - BOTTOM_PAD - BTN_H;
    }

    /** Create and register a bottom-row dialog button. */
    protected ThemedButton addDialogButton(int x, int w, Component label,
                                           GuiTheme.ButtonRole role, Button.OnPress onPress) {
        ThemedButton btn = new ThemedButton(x, buttonsY(), w, BTN_H, label, onPress, theme, role, font);
        addRenderableWidget(btn);
        return btn;
    }

    /** Extra content drawn between the body text and the buttons (e.g. a checkbox row). */
    protected void renderDialogContents(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    }

    protected void backToParent() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parentScreen);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, BACKDROP_COLOR);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        theme.drawPanel(g, left, top, panelW, panelH);
        g.centeredText(font, getTitle(), left + panelW / 2, top + TITLE_Y, theme.titleColor());
        int y = top + TEXT_Y;
        for (String line : lines) {
            g.text(font, line, left + SIDE, y, theme.textColor(), false);
            y += LINE_H;
        }
        renderDialogContents(g, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, delta);
        // Button frames on top of the widgets; ModernTheme is final and must stay
        // the button theme so ThemedButton resolves the opaque btnText color.
        if (theme instanceof ModernTheme modern) {
            ButtonFrames.render(g, this, modern.palette());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        backToParent();
    }
}
