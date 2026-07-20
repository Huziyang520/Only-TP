package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.ArrayList;

public class NetworkHandler {

    public static void register() {
        // 注册数据包类型（MC 26.2: clientboundPlay/serverboundPlay）
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPacket.TYPE, ConfigSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigUpdatePacket.TYPE, ConfigUpdatePacket.STREAM_CODEC);

        // 服务端接收配置更新（来自客户端管理员）
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePacket.TYPE, (packet, context) -> {
            context.server().execute(() -> {
                var player = context.player();

                // MC 26.2 权限检查：PermissionSet
                if (player == null || !(player.permissions() instanceof LevelBasedPermissionSet lbs
                        && lbs.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS)))
                    return;

                OnlyTPConfig.mode = packet.mode();
                OnlyTPConfig.showPauseButton = packet.showPauseButton();
                OnlyTPConfig.guiButtonStyle = packet.guiButtonStyle();
                OnlyTPConfig.blacklistAllowTp = new ArrayList<>(packet.blacklistAllowTp());
                OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(packet.blacklistBlockNonTp());
                OnlyTPConfig.blacklistDisabled = new ArrayList<>(packet.blacklistDisabled());
                OnlyTPConfig.save();

                // 广播给所有在线玩家
                var syncPacket = new ConfigSyncPacket(
                        OnlyTPConfig.mode, OnlyTPConfig.showPauseButton, OnlyTPConfig.guiButtonStyle,
                        OnlyTPConfig.blacklistAllowTp, OnlyTPConfig.blacklistBlockNonTp, OnlyTPConfig.blacklistDisabled
                );
                var server = player.level().getServer();
                if (server != null) {
                    for (var p : server.getPlayerList().getPlayers()) {
                        ServerPlayNetworking.send(p, syncPacket);
                    }
                }
            });
        });
    }

    // registerClient() 已移至 src/client/.../network/ClientNetworkHandler.java
}
