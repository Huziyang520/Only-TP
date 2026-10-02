package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import com.onlytp.onlytpmod.event.CommandLogic;
import com.onlytp.onlytpmod.event.GameModeLogic;
import com.onlytp.onlytpmod.event.GuiEventHandler;
import com.onlytp.onlytpmod.event.PlayerJoinLogic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
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
 * 26.x 已移除 Dist，直接注册（服务端不会触发客户端事件，无影响）。
 */
@Mod(Constants.MOD_ID)
public class OnlyTPMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod(IEventBus modEventBus) {
        // NeoForge 21.1 已移除 FMLCommonSetupEvent；common 初始化直接在构造中完成
        CommonClass.init();

        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Only TP Mod loaded!");

        // 26.x 已移除 Dist，直接注册（服务端不会触发客户端事件，无影响）。
        GuiEventHandler.register();
        // 模组列表 Config 按钮（原生 NeoForge 扩展点，Catalogue/Configured 同样认它）
        com.onlytp.onlytpmod.client.NeoForgeConfigScreens.register();
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        OnlyTPConfig.init();
        MinecraftServer server = event.getServer();
        // 手改 TOML 热加载后：重播配置（show_pause_button 等开关即时生效）+ 重发命令树（补全更新）
        OnlyTPConfig.setConfigChangedListener(() -> {
            PlayerJoinLogic.syncToAll(server);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                server.getCommands().sendCommands(p);
            }
        });
        LOGGER.info("Only TP config initialized");
    }

    /**
     * 服务端每 tick 检查配置热加载：手改 config/onlytp.toml 后无需命令 / 进服等触发即可重播给在线玩家。
     * 内部有 1s 节流，实际每 tick 只做一次毫秒比较。
     */
    @SubscribeEvent
    public void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        OnlyTPConfig.reloadIfChanged();
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

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerJoinLogic.onPlayerJoin(player);
        }
    }
}