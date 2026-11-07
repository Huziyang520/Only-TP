package com.onlytp.onlytpmod.event;

import com.camelot.notice.JoinNotice;
import com.camelot.ui.ConfigScreens;
import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.gui.ConfigScreenEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 客户端：暂停界面按钮 + 进世界提示。
 *
 * <p>未安装 AvalonBase 时按钮不存在；界面类名一律在 {@link ConfigScreenEntry} 内反射创建，
 * 本类不引用任何 Avalon 类型。
 */
public class GuiEventHandler {

    private static final Identifier BUTTON_TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/button.png");

    public static void register() {
        NeoForge.EVENT_BUS.addListener(GuiEventHandler::onClientPlayerLogin);
        NeoForge.EVENT_BUS.addListener(GuiEventHandler::onScreenInit);
    }

    /** 既没有 AvalonBase 也没有 Cloth 时提示玩家：可视化配置界面不可用。 */
    public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (AvalonLink.isAvalonLoaded() || ConfigScreens.clothAvailable()) {
            return;
        }
        JoinNotice.show(Minecraft.getInstance().player, OnlyTPConfig.CONFIG, "show_tips",
                "message.onlytp.notice_config_ui");
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) return;
        if (!AvalonLink.isAvalonLoaded()) return;
        if (Minecraft.getInstance().player == null) return;

        if (Minecraft.getInstance().hasSingleplayerServer()) OnlyTPConfig.CONFIG.reloadIfChanged();
        if (!OnlyTPConfig.enableConfigEntry()) return;

        int btnSize = 20;
        int x = event.getScreen().width - btnSize - 5;

        event.addListener(new Button(x, 5, btnSize, btnSize, Component.empty(),
                btn -> ConfigScreenEntry.openAvalonScreen(),
                (btn) -> Component.empty()
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
