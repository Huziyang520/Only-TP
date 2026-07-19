package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.OnlyTPMod;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;

public class NetworkHandler {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 服务端→客户端: 同步配置
        registrar.playToClient(
                ConfigSyncPacket.TYPE,
                ConfigSyncPacket.STREAM_CODEC,
                NetworkHandler::handleConfigSync
        );

        // 客户端→服务端: 更新配置
        registrar.playToServer(
                ConfigUpdatePacket.TYPE,
                ConfigUpdatePacket.STREAM_CODEC,
                NetworkHandler::handleConfigUpdate
        );
    }

    private static void handleConfigSync(ConfigSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            OnlyTPConfig.mode = packet.mode();
            OnlyTPConfig.showPauseButton = packet.showPauseButton();
            OnlyTPConfig.guiButtonStyle = packet.guiButtonStyle();
            OnlyTPConfig.blacklistAllowTp = new ArrayList<>(packet.blacklistAllowTp());
            OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(packet.blacklistBlockNonTp());
            OnlyTPConfig.blacklistDisabled = new ArrayList<>(packet.blacklistDisabled());
        });
    }

    private static void handleConfigUpdate(ConfigUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player == null || !player.hasPermissions(2)) return;

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
            var server = player.getServer();
            if (server != null) {
                for (var p : server.getPlayerList().getPlayers()) {
                    PacketDistributor.sendToPlayer(p, syncPacket);
                }
            }
        });
    }
}
