package com.onlytp.onlytpmod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ModMsg {

    private static final String MOD_ID = "onlytp";
    private static final String FALLBACK_LANG = "en_us";

    private static final String HARD_FALLBACK = "This action has been disabled by the administrator!";

    private static final Map<String, Map<String, String>> CACHE = new HashMap<>();

    private ModMsg() {
    }

    public static MutableComponent red(Player player, String key) {
        return Component.translatableWithFallback(key, fallback(player, key)).withStyle(ChatFormatting.RED);
    }

    private static String fallback(Player player, String key) {
        String lang = clientLanguage(player);
        Map<String, String> table = table(lang);
        String value = table.get(key);
        if (value == null) {
            value = table(FALLBACK_LANG).get(key);
        }
        return value != null ? stripLegacyColor(value) : HARD_FALLBACK;
    }

    private static String clientLanguage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            String language = serverPlayer.clientInformation().language();
            if (language != null && !language.isBlank()) {
                return language.toLowerCase(Locale.ROOT);
            }
        }
        return FALLBACK_LANG;
    }

    private static Map<String, String> table(String lang) {
        synchronized (CACHE) {
            Map<String, String> cached = CACHE.get(lang);
            if (cached != null) return cached;

            Map<String, String> loaded = load(lang);
            if (loaded == null) {
                loaded = FALLBACK_LANG.equals(lang) ? Map.of() : table(FALLBACK_LANG);
            }
            CACHE.put(lang, loaded);
            return loaded;
        }
    }

    private static Map<String, String> load(String lang) {
        String path = "/assets/" + MOD_ID + "/lang/" + lang + ".json";
        try (InputStream in = ModMsg.class.getResourceAsStream(path)) {
            if (in == null) return null;

            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    private static String stripLegacyColor(String value) {
        if (value.indexOf('\u00A7') < 0) return value;
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\u00A7' && i + 1 < value.length()) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
