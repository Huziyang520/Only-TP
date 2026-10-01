package com.onlytp.onlytpmod.client;

import com.onlytp.onlytpmod.event.GuiEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Fabric 客户端入口。注册暂停界面按钮注入。
 * 客户端网络接收器由 AvalonBase 的 {@code AvalonBaseClient} 统一注册。
 */
public class OnlyTPClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GuiEventHandler.register();

        // 客户端进入世界（本地玩家生成）时，若未安装 AvalonBase 则显示提示
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ClientJoinNotice.onJoinWorld()
        );
    }
}
