package com.onlytp.onlytpmod.event;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.network.ConfigSyncPacket;
import com.onlytp.onlytpmod.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class PlayerJoinHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ConfigSyncPacket(
                            OnlyTPConfig.mode, OnlyTPConfig.showPauseButton, OnlyTPConfig.guiButtonStyle,
                            OnlyTPConfig.blacklistAllowTp,
                            OnlyTPConfig.blacklistBlockNonTp,
                            OnlyTPConfig.blacklistDisabled
                    )
            );
        }
    }
}
