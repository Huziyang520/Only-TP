package com.onlytp.onlytpmod.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 模组配置。保持原版 Forge 项目的逻辑与 TOML 文件格式（config/onlytp.toml）完全不变。
 * 依赖 com.electronwill.nightconfig（TOML），该库在 Forge/Fabric 两端均为运行时依赖。
 */
public class OnlyTPConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = Paths.get("config", "onlytp.toml");

    public static String mode = "disabled";
    public static boolean showPauseButton = true;
    public static int guiButtonStyle = 0; // 0=简约风, 1=原版风
    // 客户端进入世界时若未装 AvalonBase，是否显示「安装AvalonBase启用可视化编辑」提示。0=关闭, 1=开启(默认)
    public static int showTips = 1;
    public static List<String> blacklistAllowTp = new ArrayList<>();
    public static List<String> blacklistBlockNonTp = new ArrayList<>();
    // OP 命令白名单：仅对 block_non_tp 模式的 OP 玩家生效，命中则放行（可用则用）。
    // 仅能通过 config/onlytp.toml 的 [command_whitelist] 修改，不进 GUI。
    public static List<String> commandWhitelistOp = defaultOpWhitelist();
    // 白名单热加载：记录配置文件最后修改时间，文件变更时在下次命令判定时自动重读
    private static volatile long whitelistLastModified = -1L;

    public static final String MODE_ALLOW_TP = "allow_tp_only";
    public static final String MODE_BLOCK_NON_TP = "block_non_tp";
    public static final String MODE_BOTH = "both";
    public static final String MODE_DISABLED = "disabled";

    public static void init() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.exists(CONFIG_PATH) && Files.size(CONFIG_PATH) > 0) {
                readFile();
            } else {
                resetDefaults();
                writeFile();
            }
        } catch (Exception e) {
            LOGGER.error("Fatal config error", e);
            resetDefaults();
        }
    }

    public static void readFile() {
        try {
            String text = Files.readString(CONFIG_PATH);
            Config cfg = new TomlParser().parse(text);
            mode = cfg.getOrElse("mode", MODE_DISABLED);
            showPauseButton = cfg.getOrElse("show_pause_button", true);
            guiButtonStyle = cfg.getOrElse("gui_button_style", 0);
            showTips = cfg.getOrElse("show_tips", 1);
            blacklistAllowTp = getSubList(cfg, "mode_allow_tp_only");
            blacklistBlockNonTp = getSubList(cfg, "mode_block_non_tp");
            // 白名单表不存在时使用默认值，避免旧配置（无此表）覆盖成空列表导致默认白名单失效
            commandWhitelistOp = cfg.contains("command_whitelist")
                    ? getSubList(cfg, "command_whitelist")
                    : defaultOpWhitelist();
            whitelistLastModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            LOGGER.info("Config read - mode: {}, showPauseButton: {}, guiButtonStyle: {}", mode, showPauseButton, guiButtonStyle);
        } catch (Exception e) {
            LOGGER.error("Failed to read config, using defaults", e);
            resetDefaults();
        }
    }

    public static void writeFile() {
        try {
            Config cfg = Config.inMemory();
            cfg.set("mode", mode);
            cfg.set("show_pause_button", showPauseButton);
            cfg.set("gui_button_style", guiButtonStyle);
            cfg.set("show_tips", showTips);
            setSubList(cfg, "mode_allow_tp_only", blacklistAllowTp);
            setSubList(cfg, "mode_block_non_tp", blacklistBlockNonTp);
            setSubList(cfg, "command_whitelist", commandWhitelistOp);
            String toml = new TomlWriter().writeToString(cfg);
            // 为 OP 命令白名单注入说明注释（TOML 解析时会自动忽略注释，安全）
            toml = insertWhitelistComments(toml);
            Files.writeString(CONFIG_PATH, toml);
            LOGGER.info("Config written - mode: {}, showPauseButton: {}, guiButtonStyle: {}", mode, showPauseButton, guiButtonStyle);
        } catch (Exception e) {
            LOGGER.error("Failed to write config", e);
        }
    }

    public static void save() { writeFile(); }
    public static void load() { readFile(); }

    private static List<String> getSubList(Config cfg, String section) {
        Config sub = cfg.get(section);
        if (sub == null) return new ArrayList<>();
        return new ArrayList<>(sub.getOrElse("blacklist", Collections.emptyList()));
    }

    private static void setSubList(Config cfg, String section, List<String> list) {
        Config sub = Config.inMemory();
        sub.set("blacklist", list);
        cfg.set(section, sub);
    }

    /**
     * 在 TOML 字符串的 [command_whitelist] 表前插入说明注释。
     * 仅在 writeFile 写盘时调用；readFile 使用 TomlParser 会忽略注释，不影响解析。
     */
    private static String insertWhitelistComments(String toml) {
        String marker = "[command_whitelist]";
        int idx = toml.indexOf(marker);
        if (idx < 0) return toml; // 未找到则原样返回
        String comment = "# Example: add \"kill\" below to let OP use /kill.\n";
        return toml.substring(0, idx) + comment + toml.substring(idx);
    }

    private static void resetDefaults() {
        mode = MODE_DISABLED;
        showPauseButton = true;
        guiButtonStyle = 0;
        showTips = 1;
        blacklistAllowTp = new ArrayList<>();
        blacklistBlockNonTp = new ArrayList<>();
        commandWhitelistOp = defaultOpWhitelist();
    }

    /**
     * 默认 OP 命令白名单：原版无需作弊即可使用的基础指令。
     * <ul>
     *   <li>help / ? —— 帮助列表</li>
     *   <li>me —— 第三人称动作消息</li>
     *   <li>say —— 公开聊天</li>
     *   <li>tell / msg / w —— 私聊（三个别名全列入）</li>
     *   <li>trigger —— 记分板触发器（仅Java版）</li>
     *   <li>list —— 在线玩家</li>
     *   <li>seed —— 世界种子</li>
     * </ul>
     */
    private static List<String> defaultOpWhitelist() {
        return new ArrayList<>(List.of(
                "help", "me", "say", "tell", "msg", "w", "trigger", "list", "seed"
        ));
    }

    // ─── 查询方法 ───

    public static List<String> getCurrentBlacklist() {
        return switch (mode) {
            case MODE_ALLOW_TP -> blacklistAllowTp;
            case MODE_BLOCK_NON_TP -> blacklistBlockNonTp;
            case MODE_DISABLED -> Collections.emptyList(); // disabled 模式无黑名单
            default -> blacklistAllowTp;
        };
    }

    public static boolean isBlacklisted(String playerName) {
        return getCurrentBlacklist().contains(playerName);
    }

    public static boolean isTpCommand(String commandName) {
        return "tp".equals(commandName) || "teleport".equals(commandName);
    }

    /**
     * 热加载白名单：检查配置文件是否被外部修改，若变更则重新读取 [command_whitelist] 表。
     * 通过比对文件最后修改时间实现，避免每次命令判定都重新读盘。
     */
    public static void reloadWhitelistIfChanged() {
        try {
            long modified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            if (modified == whitelistLastModified) return; // 未变更，直接返回
            whitelistLastModified = modified;
            String text = Files.readString(CONFIG_PATH);
            Config cfg = new TomlParser().parse(text);
            // 表不存在时回退默认值，表存在则采用配置（即使为空也尊重）
            commandWhitelistOp = cfg.contains("command_whitelist")
                    ? getSubList(cfg, "command_whitelist")
                    : defaultOpWhitelist();
            LOGGER.info("Command whitelist hot-reloaded: {}", commandWhitelistOp);
        } catch (Exception e) {
            LOGGER.error("Failed to hot-reload command whitelist", e);
        }
    }

    /**
     * 判断指定命令名是否命中 OP 白名单。仅由 block_non_tp 模式的 OP 玩家放行使用。
     * 判定前先尝试热加载，保证配置文件修改后无需重进世界即可生效。
     */
    public static boolean isWhitelistedForOp(String commandName) {
        reloadWhitelistIfChanged();
        return commandName != null && commandWhitelistOp.contains(commandName);
    }

    public static boolean isDisabled() { return MODE_DISABLED.equals(mode); }
    public static boolean isBlockNonTpMode() { return MODE_BLOCK_NON_TP.equals(mode); }
    public static boolean isAllowTpOnlyMode() { return MODE_ALLOW_TP.equals(mode); }
    public static boolean isBothMode() { return MODE_BOTH.equals(mode); }
}
