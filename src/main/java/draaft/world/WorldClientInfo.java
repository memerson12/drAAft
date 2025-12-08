package draaft.world;

import io.netty.buffer.Unpooled;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

/**
 * World associated data available on the client
 */
public record WorldClientInfo(
    // when adding fields remember to serialize them in buildPacket
    boolean enchantedBucket,
    boolean showCoords
) {
    public static final Identifier CHANNEL = new Identifier("draaft", "world_client_info");

    public static WorldClientInfo get(ClientWorld world) {
        var saved = ((WorldInterface) world).draaft$clientInfo();

        return saved != null
            ? saved
            : new WorldClientInfo(false, false);
    }

    public CustomPayloadS2CPacket buildPacket() {
        var buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBoolean(enchantedBucket);
        buf.writeBoolean(showCoords);

        return new CustomPayloadS2CPacket(CHANNEL, buf);
    }

    public static WorldClientInfo fromBuffer(PacketByteBuf buffer) {
        return new WorldClientInfo(
            buffer.readBoolean(),
            buffer.readBoolean()
        );
    }
}
