package com.onlytp.onlytpmod.config;

import com.camelot.config.CamelotConfig;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;
import com.camelot.config.ConfigScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Only TP 配置：服务端判定项在 {@code onlytp-common.toml}，本机偏好项在 {@code onlytp-client.toml}。
 *
 * <p>字段与默认值由 schema 声明，读写、界面与下发由 Camelot 处理，本类只提供取值与业务判定。
 */
public final class OnlyTPConfig {

    public static final String MODE_ALLOW_TP = "allow_tp_only";
    public static final String MODE_BLOCK_NON_TP = "block_non_tp";
    public static final String MODE_BOTH = "both";
    public static final String MODE_DISABLED = "disabled";

    /** 原版无需作弊即可使用的基础指令，作为 OP 命令白名单的默认值。 */
    private static final List<String> DEFAULT_OP_WHITELIST = List.of(
            "help", "me", "say", "tell", "msg", "w", "trigger", "list", "seed");

    public static final ConfigSchema COMMON_SCHEMA = new ConfigSchema("onlytp", ConfigScope.COMMON,
            "gui.onlytp.title",
            List.of(
                    ConfigOption.select("mode", MODE_DISABLED,
                            MODE_ALLOW_TP, MODE_BLOCK_NON_TP, MODE_BOTH, MODE_DISABLED),
                    ConfigOption.bool("allow_entity_selectors", true),
                    ConfigOption.bool("enable_config_entry", true).legacy("show_pause_button"),
                    ConfigOption.list("mode_allow_tp_only", List.of()),
                    ConfigOption.list("mode_block_non_tp", List.of()),
                    ConfigOption.list("command_whitelist", DEFAULT_OP_WHITELIST)
                            .comment("Example: add \"kill\" below to let OP use /kill.")));

    public static final ConfigSchema CLIENT_SCHEMA = new ConfigSchema("onlytp", ConfigScope.CLIENT,
            "gui.onlytp.title",
            List.of(
                    ConfigOption.switchInt("gui_button_style", 0),
                    ConfigOption.bool("enable_animations", true),
                    ConfigOption.switchInt("show_tips", 1)));

    /** 服务端下发、全局生效的配置。 */
    public static final CamelotConfig CONFIG = new CamelotConfig(COMMON_SCHEMA);

    /** 只影响本机的配置，不参与同步。 */
    public static final CamelotConfig CLIENT = new CamelotConfig(CLIENT_SCHEMA);

    private OnlyTPConfig() {
    }

    public static CamelotConfig[] all() {
        return new CamelotConfig[]{CONFIG, CLIENT};
    }

    // ─── 取值 ───

    public static String mode() {
        return CONFIG.getString("mode");
    }

    public static boolean allowEntitySelectors() {
        return CONFIG.getBool("allow_entity_selectors");
    }

    /** 配置入口开关：关闭后暂停界面无按钮，世界内进入入口显示"服务端已关闭"。 */
    public static boolean enableConfigEntry() {
        return CONFIG.getBool("enable_config_entry");
    }

    public static boolean enableAnimations() {
        return CLIENT.getBool("enable_animations");
    }

    public static int guiButtonStyle() {
        return CLIENT.getInt("gui_button_style");
    }

    public static List<String> blacklistAllowTp() {
        return CONFIG.getList("mode_allow_tp_only");
    }

    public static List<String> blacklistBlockNonTp() {
        return CONFIG.getList("mode_block_non_tp");
    }

    public static List<String> commandWhitelistOp() {
        return CONFIG.getList("command_whitelist");
    }

    // ─── 业务判定 ───

    public static List<String> currentBlacklist() {
        return switch (mode()) {
            case MODE_ALLOW_TP -> blacklistAllowTp();
            case MODE_BLOCK_NON_TP -> blacklistBlockNonTp();
            default -> Collections.emptyList();
        };
    }

    public static boolean isBlacklisted(String playerName) {
        return currentBlacklist().contains(playerName);
    }

    public static boolean isTpCommand(String commandName) {
        return "tp".equals(commandName) || "teleport".equals(commandName);
    }

    /** 命中 OP 命令白名单；仅由 block_non_tp 模式的 OP 玩家放行使用。 */
    public static boolean isWhitelistedForOp(String commandName) {
        return commandName != null && commandWhitelistOp().contains(commandName);
    }

    public static boolean isDisabled() {
        return MODE_DISABLED.equals(mode());
    }

    public static boolean isBlockNonTpMode() {
        return MODE_BLOCK_NON_TP.equals(mode());
    }

    public static boolean isAllowTpOnlyMode() {
        return MODE_ALLOW_TP.equals(mode());
    }

    public static boolean isBothMode() {
        return MODE_BOTH.equals(mode());
    }

    /** 界面保存后写回：立即生效并落盘（联机客户端随后由服务端下发覆盖）。 */
    public static void applyAndSave(String newMode, boolean selectors, boolean entry, boolean animations,
                                    int buttonStyle, List<String> allowTp, List<String> blockNonTp) {
        CONFIG.set("mode", newMode);
        CONFIG.set("allow_entity_selectors", selectors);
        CONFIG.set("enable_config_entry", entry);
        CONFIG.set("mode_allow_tp_only", new ArrayList<>(allowTp));
        CONFIG.set("mode_block_non_tp", new ArrayList<>(blockNonTp));
        CONFIG.save();

        CLIENT.set("enable_animations", animations);
        CLIENT.set("gui_button_style", buttonStyle);
        CLIENT.save();
    }
}
