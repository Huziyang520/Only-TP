package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.client.ClientJoinNotice;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 客户端：在暂停界面注入 OnlyTP 设置按钮，打开可视化编辑界面。
 *
 * <p>关键约束：本类字节码不得静态引用任何 Avalon 类（尤其 {@code OnlyTPScreen}）。
 * 创建 {@code OnlyTPScreen} 一律走反射（{@link #openOnlyTPScreen(Minecraft)}），
 * 所有 Avalon 联动点先经 {@link AvalonLink#isAvalonLoaded()} 守卫。
 */
public class GuiEventHandler {

    private static final ResourceLocation BUTTON_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/button.png");

    /** 由主入口在构造期调用，将本类的事件处理注册到 NeoForge 事件总线。 */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(GuiEventHandler::onClientPlayerLogin);
        NeoForge.EVENT_BUS.addListener(GuiEventHandler::onScreenInit);
    }

    /**
     * 客户端进入世界（本地玩家生成）时，若未安装 AvalonBase 则显示提示。
     */
    public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientJoinNotice.onJoinWorld();
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) return;

        // 编辑界面为「与 AvalonBase 的联动增强」：未安装 AvalonBase 时不注入按钮
        if (!AvalonLink.isAvalonLoaded()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!OnlyTPConfig.showPauseButton) return;

        int screenWidth = event.getScreen().width;
        int btnSize = 20;
        int x = screenWidth - btnSize - 5;
        int y = 5;

        event.addListener(new Button(x, y, btnSize, btnSize, Component.empty(),
                btn -> openOnlyTPScreen(mc),
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

    /**
     * 通过反射打开 {@code OnlyTPScreen}（继承 {@code AvalonConfigScreen}）。
     * 仅应在 {@code AvalonLink.isAvalonLoaded()} 为 true 时调用。
     */
    private static void openOnlyTPScreen(Minecraft mc) {
        try {
            Class<?> screenClass = Class.forName("com.onlytp.onlytpmod.gui.OnlyTPScreen");
            mc.setScreen((net.minecraft.client.gui.screens.Screen) screenClass.getConstructor().newInstance());
        } catch (Exception e) {
            Constants.LOG.error("[OnlyTP] Failed to open OnlyTPScreen via reflection", e);
        }
    }
}