package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * OnlyTP 网络通道定义。复用 AvalonBase 的 {@link com.avalon.base.network.AvalonNetwork} 抽象，
 * 各业务方向使用独立通道（Forge 按消息类分发、channel 仅占位，Fabric 以 channel 作为实际通道）。
 */
public final class NetworkChannels {

    /** 客户端 → 服务端：配置更新。 */
    public static final ResourceLocation UPDATE = new ResourceLocation(Constants.MOD_ID, "update");

    /** 服务端 → 客户端：配置同步。 */
    public static final ResourceLocation SYNC = new ResourceLocation(Constants.MOD_ID, "sync");

    private NetworkChannels() {
    }
}
