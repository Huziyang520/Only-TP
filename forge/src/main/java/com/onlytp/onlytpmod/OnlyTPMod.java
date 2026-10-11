package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.client.ForgeConfigScreens;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandRegistrationLogic;
import com.onlytp.onlytpmod.event.CommandLogic;
import com.onlytp.onlytpmod.event.GameModeLogic;
import com.onlytp.onlytpmod.event.GuiEventHandler;
import com.onlytp.onlytpmod.event.PlayerJoinLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Forge 入口。使用无参构造器，兼容 Forge 1.20.1 与 NeoForge 1.20.1（两者均支持无参构造器）。
 * 通过 FMLJavaModLoadingContext.get() 获取 mod 事件总线。
 */
@Mod(Constants.MOD_ID)
public class OnlyTPMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        // 客户端事件（暂停按钮注入、进入世界提示）只在客户端注册。
        // 不能用 @Mod.EventBusSubscriber 注解，否则 mod 构造阶段会强制加载 GuiEventHandler，
        // 进而解析其引用的 Avalon GUI 类（未装 AvalonBase 时触发 NoClassDefFoundError）。
        if (FMLEnvironment.dist == Dist.CLIENT) {
            GuiEventHandler.register();
            // 模组列表 Config 按钮（原生 Forge 扩展点，Catalogue/Configured 同样认它）
            ForgeConfigScreens.register();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 注册网络消息（经 AvalonBase 的平台无关抽象）
        CommonClass.init();
        LOGGER.info("Only TP Mod loaded!");
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
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OnlyTPConfig.reloadIfChanged();
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

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerJoinLogic.onPlayerJoin(player);
        }
    }
}
