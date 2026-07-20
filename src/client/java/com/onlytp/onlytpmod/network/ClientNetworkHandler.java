package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;

public class ClientNetworkHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPacket.TYPE, (packet, context) -> {
            context.client().execute(() -> {
                OnlyTPConfig.mode = packet.mode();
                OnlyTPConfig.showPauseButton = packet.showPauseButton();
                OnlyTPConfig.guiButtonStyle = packet.guiButtonStyle();
                OnlyTPConfig.blacklistAllowTp = new ArrayList<>(packet.blacklistAllowTp());
                OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(packet.blacklistBlockNonTp());
                OnlyTPConfig.blacklistDisabled = new ArrayList<>(packet.blacklistDisabled());
            });
        });
    }
}
