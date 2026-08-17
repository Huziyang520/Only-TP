package com.onlytp.onlytpmod.client;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.onlytp.onlytpmod.AvalonLink;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 客户端「进入世界」提示。
 * <p>当本机客户端未安装 AvalonBase（无法使用可视化编辑界面）时，在进入世界后
 * 于聊天框显示一条提示，引导玩家安装 AvalonBase 以启用可视化编辑。
 * <p>此提示可在配置文件 {@code config/onlytp.toml} 中通过 {@code show_tips}
 * 开关关闭（0=关闭，1=开启，默认 1）。
 * <p>每次进入世界（连入服务器 / 进单机世界）都会触发一次，不做一次性标记，
 * 便于玩家在安装 AvalonBase 后重进世界再次确认提示已消失。
 */
public class ClientJoinNotice {

    private static final String CONFIG_KEY = "show_tips";

    private ClientJoinNotice() {
    }

    /**
     * 进入世界时调用。判断是否需要显示提示。
     */
    public static void onJoinWorld() {
        // 已安装 AvalonBase：无需提示（可视化编辑可用）
        if (AvalonLink.isAvalonLoaded()) return;
        // 配置文件开关关闭：不显示
        if (!isShowTipsEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.displayClientMessage(
                Component.translatable("message.onlytp.avalon_missing"),
                false // false = 走聊天框，不进动作栏
        );
    }

    /**
     * 读取本机配置文件中的 {@code show_tips} 开关。
     * <p>直接解析 {@code config/onlytp.toml}，不依赖配置类是否已在客户端初始化，
     * 保证联机非主机客户端也能正确读取开关。文件不存在或字段缺失时默认开启（1）。
     */
    private static boolean isShowTipsEnabled() {
        try {
            Path path = Paths.get("config", "onlytp.toml");
            if (!Files.exists(path)) return true;
            Config cfg = new TomlParser().parse(Files.readString(path));
            int showTips = cfg.getOrElse(CONFIG_KEY, 1);
            return showTips != 0;
        } catch (Exception e) {
            // 解析失败时默认开启，避免静默吞掉提示
            return true;
        }
    }
}
