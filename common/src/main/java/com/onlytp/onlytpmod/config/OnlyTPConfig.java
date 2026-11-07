package com.onlytp.onlytpmod.config;

import com.camelot.config.CamelotConfig;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;
import com.camelot.config.ConfigScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OnlyTPConfig {

    public enum Mode {
        allow_tp_only,
        block_non_tp,
        both,
        disabled
    }

    public static final String MODE_ALLOW_TP = "allow_tp_only";
    public static final String MODE_BLOCK_NON_TP = "block_non_tp";
    public static final String MODE_BOTH = "both";
    public static final String MODE_DISABLED = "disabled";

    private static final List<String> DEFAULT_OP_WHITELIST = List.of(
            "help", "me", "say", "tell", "msg", "w", "trigger", "list", "seed");

    public static final ConfigSchema COMMON_SCHEMA = new ConfigSchema("onlytp", ConfigScope.COMMON,
            "gui.onlytp.title",
            List.of(
                    ConfigOption.select("mode", Mode.disabled),
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

    public static final CamelotConfig CONFIG = new CamelotConfig(COMMON_SCHEMA);

    public static final CamelotConfig CLIENT = new CamelotConfig(CLIENT_SCHEMA);

    private OnlyTPConfig() {
    }

    public static CamelotConfig[] all() {
        return new CamelotConfig[]{CONFIG, CLIENT};
    }

    public static String mode() {
        return CONFIG.getString("mode");
    }

    public static boolean allowEntitySelectors() {
        return CONFIG.getBool("allow_entity_selectors");
    }

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
