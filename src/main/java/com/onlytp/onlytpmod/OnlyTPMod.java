package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandHandler;
import com.onlytp.onlytpmod.event.PlayerJoinHandler;
import com.onlytp.onlytpmod.network.NetworkHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(OnlyTPMod.MODID)
public class OnlyTPMod {
    public static final String MODID = "onlytp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::commonSetup);

        // 网络处理（MOD事件总线）
        NetworkHandler.register(modEventBus);

        // 通用事件（Forge事件总线）
        NeoForge.EVENT_BUS.register(new CommandHandler());
        NeoForge.EVENT_BUS.register(new PlayerJoinHandler());
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        // 客户端专用事件
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(com.onlytp.onlytpmod.event.GuiEventHandler.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Only TP Mod loaded!");
    }

    private void onServerStarting(final ServerStartingEvent event) {
        OnlyTPConfig.init();
        LOGGER.info("Only TP config initialized");
    }
}
