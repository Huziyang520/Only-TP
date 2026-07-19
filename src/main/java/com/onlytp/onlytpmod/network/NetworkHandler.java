package com.onlytp.onlytpmod.network;

import com.onlytp.onlytpmod.OnlyTPMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(OnlyTPMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.registerMessage(id++, ConfigSyncPacket.class,
                ConfigSyncPacket::encode,
                ConfigSyncPacket::new,
                (packet, ctx) -> {
                    packet.handle(ctx.get());
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(id++, ConfigUpdatePacket.class,
                ConfigUpdatePacket::encode,
                ConfigUpdatePacket::new,
                (packet, ctx) -> {
                    packet.handle(ctx.get());
                    ctx.get().setPacketHandled(true);
                }
        );
    }
}
