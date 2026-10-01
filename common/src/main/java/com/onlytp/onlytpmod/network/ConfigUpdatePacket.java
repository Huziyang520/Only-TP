package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.AvalonLink;
import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端 → 服务端：管理员更新配置。
 * 数据结构与编解码逻辑保持与原版 Forge 项目一致；
 * 原 handle 中的 Forge 网络上下文已抽象，业务逻辑保留在 {@link #applyToServer(ServerPlayer)}。
 */
public class ConfigUpdatePacket {
    private final String mode;
    private final boolean showPauseButton;
    private final int guiButtonStyle;
    private final List<String> blacklistAllowTp;
    private final List<String> blacklistBlockNonTp;

    public ConfigUpdatePacket(String mode, boolean showPauseButton, int guiButtonStyle,
                              List<String> blA, List<String> blB) {
        this.mode = mode;
        this.showPauseButton = showPauseButton;
        this.guiButtonStyle = guiButtonStyle;
        this.blacklistAllowTp = new ArrayList<>(blA);
        this.blacklistBlockNonTp = new ArrayList<>(blB);
    }

    public ConfigUpdatePacket(FriendlyByteBuf buf) {
        this.mode = buf.readUtf();
        this.showPauseButton = buf.readBoolean();
        this.guiButtonStyle = buf.readVarInt();
        this.blacklistAllowTp = readList(buf);
        this.blacklistBlockNonTp = readList(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(mode);
        buf.writeBoolean(showPauseButton);
        buf.writeVarInt(guiButtonStyle);
        writeList(buf, blacklistAllowTp);
        writeList(buf, blacklistBlockNonTp);
    }

    /**
     * 服务端收到后处理（原 handle 内的业务逻辑，保留权限校验、写配置、广播、重发命令树）。
     */
    public void applyToServer(ServerPlayer player) {
        if (player == null) return;
        if (!player.hasPermissions(2)) return;

        OnlyTPConfig.mode = this.mode;
        OnlyTPConfig.showPauseButton = this.showPauseButton;
        OnlyTPConfig.guiButtonStyle = this.guiButtonStyle;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(this.blacklistAllowTp);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(this.blacklistBlockNonTp);
        OnlyTPConfig.save();

        var server = player.getServer();
        if (server == null) return;

        var sync = new ConfigSyncPacket(OnlyTPConfig.mode, OnlyTPConfig.showPauseButton, OnlyTPConfig.guiButtonStyle,
                OnlyTPConfig.blacklistAllowTp, OnlyTPConfig.blacklistBlockNonTp);
        if (AvalonLink.isAvalonLoaded()) {
            sendSyncToAll(server, sync);
        }

        // 重发命令树 — 配置变更后客户端补全才会更新
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(p);
        }
    }

    /**
     * 反射调用 {@code AvalonNetwork.sendToAll}，避免本类字节码静态引用 Avalon 类。
     */
    private static void sendSyncToAll(MinecraftServer server, Object sync) {
        try {
            Class<?> clazz = Class.forName("com.avalon.base.network.AvalonNetwork");
            clazz.getMethod("sendToAll", MinecraftServer.class, ResourceLocation.class, Object.class)
                    .invoke(null, server, NetworkChannels.SYNC, sync);
        } catch (Exception e) {
            com.onlytp.onlytpmod.Constants.LOG.error("[OnlyTP] Failed to broadcast sync via reflection", e);
        }
    }

    private static List<String> readList(FriendlyByteBuf buf) {
        int s = buf.readVarInt();
        List<String> l = new ArrayList<>();
        for (int i = 0; i < s; i++) l.add(buf.readUtf());
        return l;
    }

    private static void writeList(FriendlyByteBuf buf, List<String> list) {
        buf.writeVarInt(list.size());
        for (String s : list) buf.writeUtf(s);
    }
}
