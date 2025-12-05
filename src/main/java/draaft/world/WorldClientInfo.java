package draaft.world;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/// World associated data available on the client
public record WorldClientInfo(boolean enchantedBucket) {
    public static final Identifier CHANNEL = new Identifier("draaft", "world_client_info");

    public static WorldClientInfo get(World world) {
        var saved = ((WorldInterface) world).draaft$clientInfo();

        return saved != null
            ? saved
            : new WorldClientInfo(false);
    }

    public CustomPayloadS2CPacket buildPacket() {
        var buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBoolean(enchantedBucket);

        return new CustomPayloadS2CPacket(CHANNEL, buf);
    }

    public static WorldClientInfo fromBuffer(PacketByteBuf buffer) {
        return new WorldClientInfo(buffer.readBoolean());
    }
}
