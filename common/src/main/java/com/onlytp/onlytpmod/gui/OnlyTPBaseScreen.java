package com.onlytp.onlytpmod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class OnlyTPBaseScreen extends Screen {

    protected OnlyTPBaseScreen(Component title) {
        super(title);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (onlytp$scroll(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    protected abstract boolean onlytp$scroll(double mouseX, double mouseY, double delta);

    protected void onlytp$renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
    }
}
