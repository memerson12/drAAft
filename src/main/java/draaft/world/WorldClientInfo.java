package draaft.world;

import draaft.persistent.WorldManifest;
import io.netty.buffer.Unpooled;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * World associated data available on the client
 */
public record WorldClientInfo(
    // when adding fields remember to serialize them in buildPacket
    boolean enchantedBucket,
    boolean showCoords,
    WorldManifest.Annotations annotations
) {
    public static final Identifier CHANNEL = new Identifier("draaft", "world_client_info");

    public static WorldClientInfo get(ClientWorld world) {
        var saved = ((WorldInterface) world).draaft$clientInfo();

        return saved != null
            ? saved
            : new WorldClientInfo(false, false, new WorldManifest.Annotations());
    }

    public CustomPayloadS2CPacket buildPacket() {
        var buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBoolean(enchantedBucket);
        buf.writeBoolean(showCoords);
        buf.writeString(annotations.mushroomIsland != null ? annotations.mushroomIsland : "");
        buf.writeString(annotations.jungle != null ? annotations.jungle : "");
        buf.writeString(annotations.megaTaiga != null ? annotations.megaTaiga : "");
        buf.writeString(annotations.snowy != null ? annotations.snowy : "");
        buf.writeString(annotations.badlands != null ? annotations.badlands : "");
        buf.writeString(annotations.bastion != null ? annotations.bastion : "");
        buf.writeString(annotations.fortress != null ? annotations.fortress : "");

        if (annotations.strongholds != null) {
            buf.writeInt(annotations.strongholds.size());
            for (int i = 0; i < annotations.strongholds.size(); i++) {
                buf.writeString(annotations.strongholds.get(i));
            }
        } else {
            buf.writeInt(0);
        }

        return new CustomPayloadS2CPacket(CHANNEL, buf);
    }

    public static WorldClientInfo fromBuffer(PacketByteBuf buffer) {
        return new WorldClientInfo(
            buffer.readBoolean(),
            buffer.readBoolean(),
            readAnnotations(buffer)
        );
    }

    private static WorldManifest.Annotations readAnnotations(PacketByteBuf buffer) {
        WorldManifest.Annotations annotations = new WorldManifest.Annotations();
        annotations.mushroomIsland = readStringOrNull(buffer);
        annotations.jungle = readStringOrNull(buffer);
        annotations.megaTaiga = readStringOrNull(buffer);
        annotations.snowy = readStringOrNull(buffer);
        annotations.badlands = readStringOrNull(buffer);
        annotations.bastion = readStringOrNull(buffer);
        annotations.fortress = readStringOrNull(buffer);
        int length = buffer.readInt();
        List<String> strongholds = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            strongholds.add(readStringOrNull(buffer));
        }
        annotations.strongholds = strongholds;

        return annotations;
    }

    private static @Nullable String readStringOrNull(PacketByteBuf buffer) {
        String current = buffer.readString();
        return current.isEmpty() ? null : current;
    }
}
