package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.OnlyTPMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ConfigUpdatePacket(
        String mode,
        boolean showPauseButton,
        int guiButtonStyle,
        List<String> blacklistAllowTp,
        List<String> blacklistBlockNonTp,
        List<String> blacklistDisabled
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(OnlyTPMod.MODID, "config_update"));

    public static final StreamCodec<ByteBuf, ConfigUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigUpdatePacket::mode,
            ByteBufCodecs.BOOL, ConfigUpdatePacket::showPauseButton,
            ByteBufCodecs.VAR_INT, ConfigUpdatePacket::guiButtonStyle,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigUpdatePacket::blacklistAllowTp,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigUpdatePacket::blacklistBlockNonTp,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ConfigUpdatePacket::blacklistDisabled,
            ConfigUpdatePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
