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

/**
 * 聊天提示的统一出口（红色 + 兜底文案）。
 *
 * <p><b>为什么需要兜底</b>：Only TP 是服务端侧模组，{@link Component#translatable(String)}
 * 的翻译是在<b>接收端</b>解析的。接收端（客户端）没装本模组时解析不到语言文件，聊天里会
 * 直接显示原始 key（实测 {@code message.onlytp.blocked}）。因此统一改用
 * {@link Component#translatableWithFallback(String, String)}：装了模组的客户端按自己的语言
 * 显示，没装的客户端显示兜底文案。
 *
 * <p><b>兜底来源</b>：按玩家客户端上报的语言代码（{@link ServerPlayer#clientInformation()}
 * 的 language，形如 {@code zh_cn} / {@code en_us}）读取模组自带语言文件
 * {@code /assets/onlytp/lang/<lang>.json}，读不到该语言则回退 {@code en_us.json}，
 * 再读不到则回退硬编码英文——保证任何异常路径都不会把原始 key 甩给玩家。
 *
 * <p><b>颜色</b>：不再写在语言文件里（旧式 {@code §} 颜色码在第三方 UI 库下不生效，
 * 实测红色提示变白），改由 {@link ChatFormatting#RED} 组件样式给出。
 */
public final class ModMsg {

    private static final String MOD_ID = "onlytp";
    private static final String FALLBACK_LANG = "en_us";
    /** 语言文件也读不到时的最后一道保险。 */
    private static final String HARD_FALLBACK = "This action has been disabled by the administrator!";

    /** 语言代码 → （key → 文案），每个语言只解析一次。 */
    private static final Map<String, Map<String, String>> CACHE = new HashMap<>();

    private ModMsg() {
    }

    /** 红色聊天提示：优先由接收端按自己的语言解析，接收端没装模组时用同语言的兜底文案。 */
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

    /** 玩家客户端上报的语言代码；读不到（例如客户端本地玩家）时按基准语言处理。 */
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

    /** 按确切路径读语言文件——不要枚举目录（NeoForge 侧目录枚举不可靠）。 */
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

    /** 去掉旧式 {@code §x} 颜色码：兜底文案会原样进聊天，带码只会显示成乱码。 */
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
