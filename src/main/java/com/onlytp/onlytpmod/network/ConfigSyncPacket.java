package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.OnlyTPMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ConfigSyncPacket(
        String mode,
        boolean showPauseButton,
        int guiButtonStyle,
        List<String> blacklistAllowTp,
        List<String> blacklistBlockNonTp,
        List<String> blacklistDisabled
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(OnlyTPMod.MODID, "config_sync"));

    public static final StreamCodec<ByteBuf, ConfigSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigSyncPacket::mode,
            ByteBufCodecs.BOOL, ConfigSyncPacket::showPauseButton,
            ByteBufCodecs.VAR_INT, ConfigSyncPacket::guiButtonStyle,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigSyncPacket::blacklistAllowTp,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigSyncPacket::blacklistBlockNonTp,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigSyncPacket::blacklistDisabled,
            ConfigSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
