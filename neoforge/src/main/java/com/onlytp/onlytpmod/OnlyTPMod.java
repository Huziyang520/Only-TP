package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import com.onlytp.onlytpmod.event.CommandLogic;
import com.onlytp.onlytpmod.event.GameModeLogic;
import com.onlytp.onlytpmod.event.GuiEventHandler;
import com.onlytp.onlytpmod.event.PlayerJoinLogic;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * NeoForge 入口。在构造函数中注册服务端事件与网络初始化。
 *
 * <p>客户端事件（暂停按钮注入、进入世界提示）只在客户端注册。
 * 不能用 {@code @Mod.EventBusSubscriber} 注解，否则 mod 构造阶段会强制加载 GuiEventHandler，
 * 进而解析其引用的 Avalon GUI 类（未装 AvalonBase 时触发 NoClassDefFoundError）。
 */
@Mod(Constants.MOD_ID)
public class OnlyTPMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod(IEventBus modEventBus) {
        // NeoForge 21.1 已移除 FMLCommonSetupEvent；common 初始化直接在构造中完成
        CommonClass.init();

        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Only TP Mod loaded!");

        if (FMLEnvironment.dist.isClient()) {
            GuiEventHandler.register();
        }
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        OnlyTPConfig.init();
        LOGGER.info("Only TP config initialized");
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
        if (event.getEntity() instanceof ServerPlayer player) {
            GameModeLogic.onChangeGameMode(player, event.getCurrentGameMode());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerJoinLogic.onPlayerJoin(player);
        }
    }
}