package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigSyncPacket;
import com.onlytp.onlytpmod.network.NetworkChannels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 玩家登录时同步配置。
 *
 * <p><b>关键约束：本类字节码不得静态引用 {@code com.avalon.base.network.AvalonNetwork}</b>。
 * 本类在服务端玩家加入时会被加载，而 JVM 加载类时会执行字节码验证并解析常量池里
 * 引用的所有类型（含方法体内的局部类）；一旦静态引用 {@code AvalonNetwork}，在未安装
 * AvalonBase 的环境下会抛出 {@code NoClassDefFoundError}。
 * 因此这里对 {@code AvalonNetwork.sendToPlayer} 采用反射调用（{@code Class.forName} 字符串），
 * 并先经 {@link AvalonLink#isAvalonLoaded()} 守卫，未装 AvalonBase 时直接返回。
 */
public class PlayerJoinLogic {

    public static void onPlayerJoin(ServerPlayer player) {
        if (!AvalonLink.isAvalonLoaded()) return;
        try {
            Object sync = new ConfigSyncPacket(
                    OnlyTPConfig.mode, OnlyTPConfig.showPauseButton, OnlyTPConfig.guiButtonStyle,
                    OnlyTPConfig.blacklistAllowTp, OnlyTPConfig.blacklistBlockNonTp);
            Class<?> clazz = Class.forName("com.avalon.base.network.AvalonNetwork");
            clazz.getMethod("sendToPlayer", ServerPlayer.class, ResourceLocation.class, Object.class)
                    .invoke(null, player, NetworkChannels.SYNC, sync);
        } catch (Exception e) {
            com.onlytp.onlytpmod.Constants.LOG.error("[OnlyTP] Failed to sync config to player via reflection", e);
        }
    }
}
