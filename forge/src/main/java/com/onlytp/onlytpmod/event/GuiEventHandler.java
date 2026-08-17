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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;

/**
 * Forge 客户端：在暂停界面注入 OnlyTP 设置按钮，打开可视化编辑界面。
 *
 * <p><b>关键约束：本类的字节码绝对不能静态引用任何 Avalon 类</b>（尤其是继承自
 * {@code AvalonConfigScreen} 的 {@code OnlyTPScreen}）。因为本类在 mod 构造阶段就会被
 * 加载（主类构造器调用 {@link #register()}），而 JVM 加载类时会执行字节码验证，
 * 解析常量池里引用的所有类型（含方法体内的局部类）。一旦本类方法体引用了
 * {@code OnlyTPScreen}，验证器就会去加载它及其父类 {@code AvalonConfigScreen}；
 * 若客户端未安装 AvalonBase，将抛出 {@code NoClassDefFoundError} 导致整个模组加载失败。
 *
 * <p>因此这里：
 * <ul>
 *   <li>用 {@code EVENT_BUS.addListener(方法引用)} 注册（不做 ASM 方法体织入）；</li>
 *   <li>创建 {@code OnlyTPScreen} 一律走反射（{@link #openOnlyTPScreen(Minecraft)}），
 *       本类字节码不再引用该 Avalon 继承类；</li>
 *   <li>所有 Avalon 联动点先经 {@link AvalonLink#isAvalonLoaded()} 守卫，未装 AvalonBase
 *       时直接返回，永不触发反射加载 Avalon 类。</li>
 * </ul>
 */
public class GuiEventHandler {

    private static final ResourceLocation BUTTON_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/button.png");

    /** 由主入口在构造期调用，将本类的事件处理注册到 Forge 事件总线。 */
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(GuiEventHandler::onClientPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(GuiEventHandler::onScreenInit);
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
