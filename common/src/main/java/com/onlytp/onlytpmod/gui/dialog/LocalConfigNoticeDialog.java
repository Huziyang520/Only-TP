package com.onlytp.onlytpmod.gui.dialog;

import com.avalon.base.gui.theme.GuiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Consumer;

/**
 * Confirmation dialog opened from the mod list before editing the local
 * (main-menu) configuration.
 *
 * <p>The "don't show again" checkbox occupies its own row above the buttons
 * (it must never share a line with the buttons, which broke the layout for
 * long English labels) and is rendered with the standard toggle visual:
 * a 10x10 box with a solid inner fill block, identical to the toggles on the
 * editor screens.</p>
 *
 * <p>The checkbox state is held purely in this dialog: it is reported to the
 * caller only when the player presses confirm, so cancelling never persists
 * anything. Constructor signature matches the old AvalonBase dialog so the
 * reflective entry point only swaps the class name.</p>
 */
public final class LocalConfigNoticeDialog extends EnderDialog {

    private static final int BTN_W = 72;
    private static final int BOX = 10;
    private static final int LABEL_GAP = 24;
    private static final int BUTTON_GAP = 8;

    private final Component checkboxLabel;
    private final Component cancelLabel;
    private final Component confirmLabel;
    private final Consumer<Boolean> onConfirm;

    private boolean checked;
    private boolean fired;

    public LocalConfigNoticeDialog(Screen parent, Component title, Component message, Component checkboxLabel,
                                   Component cancelLabel, Component confirmLabel, Consumer<Boolean> onConfirm) {
        super(parent, title, message, EnderBlue.THEME);
        this.checkboxLabel = checkboxLabel;
        this.cancelLabel = cancelLabel;
        this.confirmLabel = confirmLabel;
        this.onConfirm = onConfirm;
    }

    @Override
    protected int minPanelH() {
        return 112;
    }

    @Override
    protected int bottomHeight() {
        // Independent checkbox row + independent button row.
        return ROW_GAP + BOX + ROW_GAP + BTN_H + BOTTOM_PAD;
    }

    @Override
    protected void addDialogWidgets() {
        int confirmX = left + panelW - SIDE - BTN_W;
        int cancelX = confirmX - BUTTON_GAP - BTN_W;
        addDialogButton(cancelX, BTN_W, cancelLabel, GuiTheme.ButtonRole.NEUTRAL, b -> backToParent());
        addDialogButton(confirmX, BTN_W, confirmLabel, GuiTheme.ButtonRole.PRIMARY, b -> doConfirm());
    }

    private int boxX() {
        return left + SIDE;
    }

    /** Top Y of the 10x10 checkbox: its own row, one gap above the button row. */
    private int boxY() {
        return buttonsY() - ROW_GAP - BOX;
    }

    private boolean overCheckbox(double mx, double my) {
        int bx = boxX();
        int by = boxY();
        int w = LABEL_GAP + font.width(checkboxLabel);
        return mx >= bx - 3 && mx <= bx + w + 3 && my >= by - 3 && my <= by + BOX + 3;
    }

    @Override
    protected void renderDialogContents(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int bx = boxX();
        int by = boxY();
        // Same 10x10 box + solid fill block used by the editor toggles.
        theme.drawToggle(g, bx, by, checked ? 1.0F : 0.0F, true);
        boolean hover = overCheckbox(mouseX, mouseY);
        int labelColor = hover ? theme.titleColor() : theme.textColor();
        g.text(font, checkboxLabel, bx + LABEL_GAP, by + 1, labelColor, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && overCheckbox(event.x(), event.y())) {
            this.checked = !this.checked;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            doConfirm();
            return true;
        }
        return super.keyPressed(event);
    }

    private void doConfirm() {
        if (fired) {
            return;
        }
        fired = true;
        onConfirm.accept(checked);
    }
}
