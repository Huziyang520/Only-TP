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

/**
 * Fabric 入口。
 * <p>命令执行拦截与游戏模式切换拦截由 mixin 完成（见 mixin 包）；配置读写、界面、
 * 服务端下发与热加载由 Camelot 处理，这里只登记本模组的配置与"配置变化后重发命令树"。
 */
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

    /** 配置变化后重发命令树，客户端补全随之更新。 */
    private static void resendCommands(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }
}
