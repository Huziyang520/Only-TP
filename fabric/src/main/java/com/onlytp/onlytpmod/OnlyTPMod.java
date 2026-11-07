package com.onlytp.onlytpmod;

import com.camelot.fabric.FabricConfigs;
import com.camelot.net.CamelotNet;
import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class OnlyTPMod implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        FabricConfigs.register(OnlyTPConfig.all());
        CamelotNet.register(OnlyTPConfig.CONFIG, OnlyTPMod::resendCommands);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CommandRegistrationLogic.patch(dispatcher.getRoot()));

        LOGGER.info("Only TP Mod loaded!");
    }

    private static void resendCommands(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }
}
