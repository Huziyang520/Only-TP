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
    // 非OP提权指令是否允许使用目标选择器（@a/@p/@e 等）；关闭 = 客户端不补全、服务端拒绝提权执行
    public static boolean allowEntitySelectors = true;
    public static boolean showPauseButton = true;
    // 界面是否播放开/关动画（AvalonBase 动画 API；默认开启）
    public static boolean enableAnimations = true;
    public static int guiButtonStyle = 0; // 0=简约风, 1=原版风
    // 客户端进入世界时若未装 AvalonBase，是否显示「安装AvalonBase启用可视化编辑」提示。0=关闭, 1=开启(默认)
    public static int showTips = 1;
    // Client-side only: whether the main-menu local-config notice dialog shows. Never synced.
    public static int showLocalConfigNotice = 1;
    public static List<String> blacklistAllowTp = new ArrayList<>();
    public static List<String> blacklistBlockNonTp = new ArrayList<>();
    // OP 命令白名单：仅对 block_non_tp 模式的 OP 玩家生效，命中则放行（可用则用）。
    // 仅能通过 config/onlytp.toml 的 [command_whitelist] 修改，不进 GUI。
    public static List<String> commandWhitelistOp = defaultOpWhitelist();
    // 配置热加载：记录配置文件最后修改时间，文件变更时在下次判定时自动重读
    private static volatile long whitelistLastModified = -1L;

    /**
     * 热加载确认配置文件被外部修改后，由平台入口注入的回调：向在线玩家重播配置同步包。
     * config 层无服务端引用，用回调解耦（与 AuthCmdConfig#setConfigBroadcaster 同思路）。
     */
    private static volatile Runnable configChangedListener;

    /**
     * 是否已初始化（用于热加载判定）。仅服务端入口（含单机集成服务端）调用 {@link #init()} 时置位；
     * 联机客户端不置位，从而不会去读本地 toml 覆盖服务端 SYNC 下来的值。
     */
    private static volatile boolean initialized = false;

    /** 热加载最小检查间隔（毫秒）。消费点（暂停页注入 / 入口判定）与每 tick 检查都可能调用，需节流。 */
    private static final long RELOAD_CHECK_INTERVAL_MS = 1000L;

    /** 上次真正执行文件 stat 的时刻。 */
    private static volatile long lastCheckTime = 0L;

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
        initialized = true;
    }

    public static void readFile() {
        try {
            String text = Files.readString(CONFIG_PATH);
            Config cfg = new TomlParser().parse(text);
            mode = cfg.getOrElse("mode", MODE_DISABLED);
            allowEntitySelectors = cfg.getOrElse("allow_entity_selectors", true);
            showPauseButton = cfg.getOrElse("show_pause_button", true);
            guiButtonStyle = cfg.getOrElse("gui_button_style", 0);
            enableAnimations = cfg.getOrElse("enable_animations", true);
            showTips = cfg.getOrElse("show_tips", 1);
            showLocalConfigNotice = cfg.getOrElse("show_local_config_notice", 1);
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
            // 写盘顺序即文件里的顺序：模式与行为 → 界面 → 提示 → 名单子表
            // （子表必须放在所有标量之后，否则 TOML 会把先写的标量并进子表）
            cfg.set("mode", mode);
            cfg.set("allow_entity_selectors", allowEntitySelectors);
            cfg.set("show_pause_button", showPauseButton);
            cfg.set("gui_button_style", guiButtonStyle);
            cfg.set("enable_animations", enableAnimations);
            cfg.set("show_tips", showTips);
            cfg.set("show_local_config_notice", showLocalConfigNotice);
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
        allowEntitySelectors = true;
        showPauseButton = true;
        guiButtonStyle = 0;
        enableAnimations = true;
        showTips = 1;
        showLocalConfigNotice = 1;
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
     * 热加载配置：检查配置文件是否被外部修改，若变更则<b>全量重读</b>。
     * 通过比对文件最后修改时间实现，避免每次判定都重新读盘。
     *
     * <p>早期实现只重读 {@code [command_whitelist]}，导致 {@code show_pause_button}
     * 这类非命令类开关无法热生效（表现为"由 true 改 false 立刻消失、由 false 改 true 不回来"）。
     * 现改为整表重读，并在变更后触发 {@link #configChangedListener} 让服务端把配置重播给在线客户端。
     */
    public static void reloadIfChanged() {
        if (!initialized) return; // 联机客户端：配置以服务端 SYNC 为准，不读本地 toml
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < RELOAD_CHECK_INTERVAL_MS) return;
        lastCheckTime = now;
        try {
            long modified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            if (modified == whitelistLastModified) return; // 未变更，直接返回
            readFile(); // 全量重读（含 command_whitelist / show_pause_button / gui_button_style / mode 等）
            LOGGER.info("Config hot-reloaded - whitelist: {}, showPauseButton: {}, guiButtonStyle: {}",
                    commandWhitelistOp, showPauseButton, guiButtonStyle);
            Runnable listener = configChangedListener;
            if (listener != null) listener.run();
        } catch (Exception e) {
            LOGGER.error("Failed to hot-reload config", e);
        }
    }

    /**
     * 旧名保留（命令判定路径调用），语义等同 {@link #reloadIfChanged()}。
     */
    public static void reloadWhitelistIfChanged() {
        reloadIfChanged();
    }

    /**
     * 注入配置热加载回调。仅服务端入口在服务器启动后调用一次；纯客户端场景留空。
     */
    public static void setConfigChangedListener(Runnable listener) {
        configChangedListener = listener;
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