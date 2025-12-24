package draaft.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import draaft.draaft;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.io.IOException;

public record PlayerData(boolean hasCompletedGreatView) {
    public static final TrackedData<PlayerData> TRACKED = DataTracker.registerData(PlayerEntity.class, Handler.INSTANCE);

    public static final PlayerData DEFAULT = new PlayerData(false);

    private static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.BOOL
                .fieldOf("hasCompletedGreatView")
                .withDefault(false)
                .forGetter(PlayerData::hasCompletedGreatView)
        ).apply(instance, PlayerData::new)
    );

    public PlayerData withHasCompletedGreatView(boolean value) {
        return new PlayerData(value);
    }

    private static class Handler implements TrackedDataHandler<PlayerData> {
        public static Handler INSTANCE = new Handler();

        static {
            TrackedDataHandlerRegistry.register(INSTANCE);
        }

        @Override
        public void write(PacketByteBuf packet, PlayerData data) {
            try {
                packet.encode(CODEC, data);
            } catch (IOException e) {
                draaft.LOGGER.error("failed to encode player data");
            }
        }

        @Override
        public PlayerData read(PacketByteBuf packet) {
            try {
                return packet.decode(CODEC);
            } catch (IOException e) {
                draaft.LOGGER.error("failed to decode player data");
                return DEFAULT;
            }
        }

        @Override
        public PlayerData copy(PlayerData data) {
            return data;
        }
    }
}
