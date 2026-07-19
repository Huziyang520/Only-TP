package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.config.OnlyTPConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ConfigUpdatePacket {
    private final String mode;
    private final boolean showPauseButton;
    private final int guiButtonStyle;
    private final List<String> blacklistAllowTp;
    private final List<String> blacklistBlockNonTp;
    private final List<String> blacklistDisabled;

    public ConfigUpdatePacket(String mode, boolean showPauseButton, int guiButtonStyle,
                              List<String> blA, List<String> blB, List<String> blC) {
        this.mode = mode;
        this.showPauseButton = showPauseButton;
        this.guiButtonStyle = guiButtonStyle;
        this.blacklistAllowTp = new ArrayList<>(blA);
        this.blacklistBlockNonTp = new ArrayList<>(blB);
        this.blacklistDisabled = new ArrayList<>(blC);
    }

    public ConfigUpdatePacket(FriendlyByteBuf buf) {
        this.mode = buf.readUtf();
        this.showPauseButton = buf.readBoolean();
        this.guiButtonStyle = buf.readVarInt();
        this.blacklistAllowTp = readList(buf);
        this.blacklistBlockNonTp = readList(buf);
        this.blacklistDisabled = readList(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(mode);
        buf.writeBoolean(showPauseButton);
        buf.writeVarInt(guiButtonStyle);
        writeList(buf, blacklistAllowTp);
        writeList(buf, blacklistBlockNonTp);
        writeList(buf, blacklistDisabled);
    }

    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!player.hasPermissions(2)) return;

            OnlyTPConfig.mode = this.mode;
            OnlyTPConfig.showPauseButton = this.showPauseButton;
            OnlyTPConfig.guiButtonStyle = this.guiButtonStyle;
            OnlyTPConfig.blacklistAllowTp = new ArrayList<>(this.blacklistAllowTp);
            OnlyTPConfig.blacklistBlockNonTp = new ArrayList<>(this.blacklistBlockNonTp);
            OnlyTPConfig.blacklistDisabled = new ArrayList<>(this.blacklistDisabled);
            OnlyTPConfig.save();

            var sync = new ConfigSyncPacket(OnlyTPConfig.mode, OnlyTPConfig.showPauseButton, OnlyTPConfig.guiButtonStyle,
                    OnlyTPConfig.blacklistAllowTp, OnlyTPConfig.blacklistBlockNonTp, OnlyTPConfig.blacklistDisabled);
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), sync);
        });
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
