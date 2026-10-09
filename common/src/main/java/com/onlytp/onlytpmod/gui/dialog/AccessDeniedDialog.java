package com.onlytp.onlytpmod.gui.dialog;

import com.avalon.base.gui.theme.GuiTheme;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Modal dialog shown when the remote server has closed the config editing
 * entry. It carries no business logic: a single button (or the enter/escape
 * key) returns the player to the parent screen.
 *
 * <p>Constructor signature is intentionally identical to the old
 * {@code com.avalon.base.gui.dialog.AccessDeniedDialog} so the reflective
 * entry point only has to swap the class name.</p>
 */
public final class AccessDeniedDialog extends EnderDialog {

    private static final int BTN_W = 80;
    private static final int GAP_ABOVE_BUTTON = 10;

    private final Component buttonLabel;

    public AccessDeniedDialog(Screen parent, Component title, Component message, Component buttonLabel) {
        super(parent, title, message, EnderBlue.THEME);
        this.buttonLabel = buttonLabel;
    }

    @Override
    protected int minPanelH() {
        return 96;
    }

    @Override
    protected int bottomHeight() {
        return GAP_ABOVE_BUTTON + BTN_H + BOTTOM_PAD;
    }

    @Override
    protected void addDialogWidgets() {
        int bx = left + (panelW - BTN_W) / 2;
        addDialogButton(bx, BTN_W, buttonLabel, GuiTheme.ButtonRole.NEUTRAL, b -> backToParent());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 1.21.8：主键盘 ENTER=257 / 小键盘 ENTER=335（等价原版的“确认键”）
        if (keyCode == 257 || keyCode == 335) {
            backToParent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
