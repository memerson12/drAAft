package draaft.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public record HealthResetS2CPacket(float health) {
    public static final Identifier CHANNEL = new Identifier("draaft", "health_reset");

    public CustomPayloadS2CPacket buildPacket() {
        var buf = new PacketByteBuf(Unpooled.buffer(4));

        buf.writeFloatLE(this.health);

        return new CustomPayloadS2CPacket(CHANNEL, buf);
    }

    public static HealthResetS2CPacket fromBuffer(PacketByteBuf buffer) {
        return new HealthResetS2CPacket(buffer.readFloatLE());
    }
}
