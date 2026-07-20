package com.onlytp.onlytpmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置管理 — 使用 Gson (JSON) 读写，避免外部 TOML 依赖。
 */
public class OnlyTPConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = Paths.get("config", "onlytp.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
            Map<String, Object> map = GSON.fromJson(text,
                    new TypeToken<Map<String, Object>>() {}.getType());
            if (map == null) { resetDefaults(); return; }

            mode = stringVal(map.get("mode"), MODE_DISABLED);
            showPauseButton = boolVal(map.get("show_pause_button"), true);
            guiButtonStyle = intVal(map.get("gui_button_style"), 0);
            blacklistAllowTp = listVal(map.get("blacklist_allow_tp"));
            blacklistBlockNonTp = listVal(map.get("blacklist_block_non_tp"));
            blacklistDisabled = listVal(map.get("blacklist_disabled"));

            LOGGER.info("Config read - mode: {}, showPauseButton: {}, guiButtonStyle: {}",
                    mode, showPauseButton, guiButtonStyle);
        } catch (Exception e) {
            LOGGER.error("Failed to read config, using defaults", e);
            resetDefaults();
        }
    }

    public static void writeFile() {
        try {
            var map = Map.of(
                    "mode", mode,
                    "show_pause_button", showPauseButton,
                    "gui_button_style", guiButtonStyle,
                    "blacklist_allow_tp", blacklistAllowTp,
                    "blacklist_block_non_tp", blacklistBlockNonTp,
                    "blacklist_disabled", blacklistDisabled
            );
            String json = GSON.toJson(map);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config written - mode: {}, showPauseButton: {}, guiButtonStyle: {}",
                    mode, showPauseButton, guiButtonStyle);
        } catch (Exception e) {
            LOGGER.error("Failed to write config", e);
        }
    }

    public static void save() { writeFile(); }
    public static void load() { readFile(); }

    private static void resetDefaults() {
        mode = MODE_DISABLED;
        showPauseButton = true;
        guiButtonStyle = 0;
        blacklistAllowTp = new ArrayList<>();
        blacklistBlockNonTp = new ArrayList<>();
        blacklistDisabled = new ArrayList<>();
    }

    // ─── JSON 取值辅助 ───

    @SuppressWarnings("unchecked")
    private static String stringVal(Object v, String def) {
        return v instanceof String s ? s : def;
    }

    private static boolean boolVal(Object v, boolean def) {
        return v instanceof Boolean b ? b : def;
    }

    private static int intVal(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    @SuppressWarnings("unchecked")
    private static List<String> listVal(Object v) {
        if (v instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object e : list) {
                if (e instanceof String s) result.add(s);
            }
            return result;
        }
        return new ArrayList<>();
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
