package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.OnlyTPMod;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.OnlyTPScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class GuiEventHandler {

    private static final ResourceLocation BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OnlyTPMod.MODID, "textures/gui/button.png");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!OnlyTPConfig.showPauseButton) return;

        int screenWidth = event.getScreen().width;
        int btnSize = 20;
        int x = screenWidth - btnSize - 5;
        int y = 5;

        event.addListener(new Button(x, y, btnSize, btnSize, Component.empty(),
                btn -> mc.setScreen(new OnlyTPScreen()),
                (btn) -> Component.empty()
        ) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                int ox = getX() + (getWidth() - 16) / 2;
                int oy = getY() + (getHeight() - 16) / 2;
                graphics.blit(BUTTON_TEXTURE, ox, oy, 0, 0, 16, 16, 16, 16);
            }
        });
    }
}
