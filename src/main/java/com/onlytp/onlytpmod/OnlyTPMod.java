package com.onlytp.onlytpmod;

import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.PlayerJoinHandler;
import com.onlytp.onlytpmod.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class OnlyTPMod implements ModInitializer {
    public static final String MODID = "onlytp";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // 提前初始化配置（而非在 SERVER_STARTED 中阻塞服务端主循环）
        OnlyTPConfig.init();
        LOGGER.info("Only TP config initialized");

        // 网络注册
        NetworkHandler.register();

        // 玩家加入事件
        PlayerJoinHandler.register();

        LOGGER.info("Only TP Mod loaded!");
    }
}
