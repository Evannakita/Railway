package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.util.packet.JukeboxCartPacket;
import com.railwayteam.railways.util.packet.MountedToolboxSyncPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import static net.minecraftforge.network.NetworkDirection.*;

public enum CRPackets {
  // Client to Server

  // Server to Client
  JUKEBOX_CART_UPDATE(JukeboxCartPacket.class, JukeboxCartPacket::new, PLAY_TO_CLIENT),
  MOUNTED_TOOLBOX_SYNC(MountedToolboxSyncPacket.class, MountedToolboxSyncPacket::new, PLAY_TO_CLIENT)
  ;
  public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Railways.MODID, "main");
  public static final int PROTOCOL_VER = 2;
  public static final String PROTOCOL_VER_STR = String.valueOf(PROTOCOL_VER);
  public static SimpleChannel channel;

  private LoadedPacket<?> packet;

  <T extends SimplePacketBase> CRPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
                                          NetworkDirection direction) {
    packet = new LoadedPacket<>(type, factory, direction);
  }

  public static void registerPackets() {
    channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_ID)
        .serverAcceptedVersions(PROTOCOL_VER_STR::equals)
        .clientAcceptedVersions(PROTOCOL_VER_STR::equals)
        .networkProtocolVersion(() -> PROTOCOL_VER_STR)
        .simpleChannel();
    for (CRPackets packet : values())
      packet.packet.register();
  }

  public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
    channel.send(
        PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.dimension())),
        message);
  }

  private static class LoadedPacket<T extends SimplePacketBase> {
    private static int index = 0;

    private BiConsumer<T, FriendlyByteBuf> encoder;
    private Function<FriendlyByteBuf, T> decoder;
    private BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
    private Class<T> type;
    private NetworkDirection direction;

    private LoadedPacket(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
      encoder = T::write;
      decoder = factory;
      handler = T::handle;
      this.type = type;
      this.direction = direction;
    }

    private void register() {
      channel.messageBuilder(type, index++, direction)
          .encoder(encoder)
          .decoder(decoder)
          .consumer(handler)
          .add();
    }
  }
}
