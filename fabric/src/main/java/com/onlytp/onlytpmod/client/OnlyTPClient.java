package com.onlytp.onlytpmod.client;

import com.camelot.notice.JoinNotice;
import com.camelot.ui.ConfigScreens;
import com.mojang.logging.LogUtils;
import com.onlytp.onlytpmod.Constants;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import com.onlytp.onlytpmod.event.GuiEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/** Fabric 客户端入口：暂停界面按钮 + 进世界提示。 */
public class OnlyTPClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GuiEventHandler.register();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoinWorld());
    }

    /** 本机没有任何配置界面（未装 Cloth Config API）时提示一次。 */
    private static void onJoinWorld() {
        if (ConfigScreens.available(Constants.MOD_ID)) {
            return;
        }
        JoinNotice.show(Minecraft.getInstance().player, OnlyTPConfig.CLIENT, "show_tips",
                "message.onlytp.notice_config_ui");
    }
}
