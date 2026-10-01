package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import com.onlytp.onlytpmod.event.PlayerJoinLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Fabric 入口。替代原版 Forge OnlyTPMod 的入口职责。
 * 命令执行拦截与游戏模式切换拦截由 Fabric mixin 完成（见 mixin 包）。
 */
public class OnlyTPMod implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // 注册网络消息（经 AvalonBase 的平台无关抽象）
        CommonClass.init();
        LOGGER.info("Only TP Mod loaded!");

        // 服务端启动时初始化配置（对应原版 ServerStartingEvent）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            OnlyTPConfig.init();
            LOGGER.info("Only TP config initialized");
        });

        // 命令注册后 patch tp/teleport 的 requirement（对应原版 RegisterCommandsEvent）
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandRegistrationLogic.patch(dispatcher.getRoot());
        });

        // 玩家加入时同步配置（对应原版 PlayerLoggedInEvent）
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            PlayerJoinLogic.onPlayerJoin(player);
        });
    }
}
