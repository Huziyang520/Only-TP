package com.onlytp.onlytpmod;

import com.camelot.config.ConfigEnums;
import com.camelot.net.CamelotNet;
import com.camelot.neoforge.NeoConfigs;
import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.client.NeoForgeConfigScreens;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandLogic;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import com.onlytp.onlytpmod.event.GameModeLogic;
import com.onlytp.onlytpmod.event.GuiEventHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

/**
 * NeoForge 入口。
 * <p>不能用 {@code @Mod.EventBusSubscriber}：mod 构造阶段会强制加载事件类，进而解析其
 * 引用的客户端类型（未装 AvalonBase 时 {@code NoClassDefFoundError}）。
 */
@Mod(Constants.MOD_ID)
public class OnlyTPMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod(IEventBus modEventBus) {
        ConfigEnums.register(Constants.MOD_ID, "mode", com.onlytp.onlytpmod.config.NeoMode.class);
        NeoConfigs.register(OnlyTPConfig.all());
        CamelotNet.register(OnlyTPConfig.CONFIG, OnlyTPMod::resendCommands);

        NeoForge.EVENT_BUS.register(this);
        GuiEventHandler.register();
        NeoForgeConfigScreens.register();

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

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (CommandLogic.shouldCancel(event.getParseResults())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistrationLogic.patch(event.getDispatcher().getRoot());
    }

    @SubscribeEvent
    public void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && GameModeLogic.shouldBlock(player, event.getCurrentGameMode())) {
            event.setCanceled(true);
        }
    }
}
