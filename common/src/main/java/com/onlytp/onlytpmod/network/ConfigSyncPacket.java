package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端 → 客户端：同步配置。
 * 数据结构与编解码逻辑保持与原版 Forge 项目一致；
 * 原 handle 中的 Forge NetworkEvent.Context 已移除，改为纯业务方法 applyToClient()。
 */
public class ConfigSyncPacket {
    private final String mode;
    private final boolean allowEntitySelectors;
    private final boolean showPauseButton;
    private final boolean enableAnimations;
    private final int guiButtonStyle;
    private final List<String> blacklistAllowTp;
    private final List<String> blacklistBlockNonTp;

    public ConfigSyncPacket(String mode, boolean allowEntitySelectors, boolean showPauseButton, boolean enableAnimations,
                            int guiButtonStyle, List<String> blA, List<String> blB) {
        this.mode = mode;
        this.allowEntitySelectors = allowEntitySelectors;
        this.showPauseButton = showPauseButton;
        this.enableAnimations = enableAnimations;
        this.guiButtonStyle = guiButtonStyle;
        this.blacklistAllowTp = new ArrayList<>(blA);
        this.blacklistBlockNonTp = new ArrayList<>(blB);
    }

    public ConfigSyncPacket(FriendlyByteBuf buf) {
        this.mode = buf.readUtf();
        this.allowEntitySelectors = buf.readBoolean();
        this.showPauseButton = buf.readBoolean();
        this.enableAnimations = buf.readBoolean();
        this.guiButtonStyle = buf.readVarInt();
        this.blacklistAllowTp = readList(buf);
        this.blacklistBlockNonTp = readList(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(mode);
        buf.writeBoolean(allowEntitySelectors);
        buf.writeBoolean(showPauseButton);
        buf.writeBoolean(enableAnimations);
        buf.writeVarInt(guiButtonStyle);
        writeList(buf, blacklistAllowTp);
        writeList(buf, blacklistBlockNonTp);
    }

    /**
     * 客户端收到后应用配置（原 handle 内的业务逻辑）。
     */
    public void applyToClient() {
        OnlyTPConfig.mode = this.mode;
        OnlyTPConfig.allowEntitySelectors = this.allowEntitySelectors;
        OnlyTPConfig.showPauseButton = this.showPauseButton;
        OnlyTPConfig.enableAnimations = this.enableAnimations;
        OnlyTPConfig.guiButtonStyle = this.guiButtonStyle;
        OnlyTPConfig.blacklistAllowTp = new ArrayList<>(this.blacklistAllowTp);
        OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(this.blacklistBlockNonTp);
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