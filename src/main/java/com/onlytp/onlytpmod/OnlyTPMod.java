package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.CommandHandler;
import com.onlytp.onlytpmod.event.PlayerJoinHandler;
import com.onlytp.onlytpmod.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OnlyTPMod.MODID)
public class OnlyTPMod {
    public static final String MODID = "onlytp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OnlyTPMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(new CommandHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
        LOGGER.info("Only TP Mod loaded!");
    }

    private void onServerStarting(final ServerStartingEvent event) {
        OnlyTPConfig.init();
        LOGGER.info("Only TP config initialized");
    }
}
