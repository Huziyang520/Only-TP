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

public class OnlyTPConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = Paths.get("config", "onlytp.toml");

    public static String mode = "disabled";
    public static boolean showPauseButton = true;
    public static int guiButtonStyle = 0; // 0=简约风, 1=原版风
    public static List<String> blacklistAllowTp = new ArrayList<>();
    public static List<String> blacklistBlockNonTp = new ArrayList<>();
    public static List<String> blacklistDisabled = new ArrayList<>();

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
            blacklistAllowTp = getSubList(cfg, "mode_allow_tp_only");
            blacklistBlockNonTp = getSubList(cfg, "mode_block_non_tp");
            blacklistDisabled = getSubList(cfg, "mode_disabled");
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
            setSubList(cfg, "mode_allow_tp_only", blacklistAllowTp);
            setSubList(cfg, "mode_block_non_tp", blacklistBlockNonTp);
            setSubList(cfg, "mode_disabled", blacklistDisabled);
            String toml = new TomlWriter().writeToString(cfg);
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

    private static void resetDefaults() {
        mode = MODE_DISABLED;
        showPauseButton = true;
        guiButtonStyle = 0;
        blacklistAllowTp = new ArrayList<>();
        blacklistBlockNonTp = new ArrayList<>();
        blacklistDisabled = new ArrayList<>();
    }

    // ─── 查询方法 ───

    public static List<String> getCurrentBlacklist() {
        return switch (mode) {
            case MODE_ALLOW_TP -> blacklistAllowTp;
            case MODE_BLOCK_NON_TP -> blacklistBlockNonTp;
            case MODE_DISABLED -> blacklistDisabled;
            default -> blacklistAllowTp;
        };
    }

    public static boolean isBlacklisted(String playerName) {
        return getCurrentBlacklist().contains(playerName);
    }

    public static boolean isTpCommand(String commandName) {
        return "tp".equals(commandName) || "teleport".equals(commandName);
    }

    public static boolean isDisabled() { return MODE_DISABLED.equals(mode); }
    public static boolean isBlockNonTpMode() { return MODE_BLOCK_NON_TP.equals(mode); }
    public static boolean isAllowTpOnlyMode() { return MODE_ALLOW_TP.equals(mode); }
    public static boolean isBothMode() { return MODE_BOTH.equals(mode); }
}
