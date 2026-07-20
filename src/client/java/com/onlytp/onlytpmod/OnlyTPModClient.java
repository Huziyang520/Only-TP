package com.onlytp.onlytpmod;

import com.onlytp.onlytpmod.event.GuiEventHandler;
import com.onlytp.onlytpmod.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class OnlyTPModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 客户端网络接收器
        ClientNetworkHandler.register();

        // GUI 事件（暂停界面按钮）
        GuiEventHandler.register();
    }
}
