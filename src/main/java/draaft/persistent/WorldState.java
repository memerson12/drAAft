package draaft.persistent;

import draaft.draaft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class WorldState extends PersistentState {
    private long test;
    private Random pearlRng;

    public WorldState(String key) {
        super(key);
        this.test = 0L;
    }

    public static WorldState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new WorldState("draaft_world_state"),
                "draaft_world_state");
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.test = tag.getLong("test");

        if (tag.contains("pearl_rng")) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(tag.getByteArray("pearl_rng"));
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                this.pearlRng = (Random) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // If deserialization fails, fall back to just using the seed
                if (tag.contains("pearl_seed")) {
                    this.pearlRng = new Random(tag.getLong("pearl_seed"));
                }
            }
        } else if (tag.contains("pearl_seed")) {
            this.pearlRng = new Random(tag.getLong("pearl_seed"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        nbt.putLong("test", this.test);

        if (this.pearlRng != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(this.pearlRng);
                nbt.putByteArray("pearl_rng", baos.toByteArray());
            } catch (IOException e) {
                // If serialization fails, fall back to just storing the seed
                nbt.putLong("pearl_seed", this.pearlRng.nextLong());
            }
        }

        return nbt;
    }

    public Random getOrCreatePearlRng(ServerWorld world) {
        if (this.pearlRng == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.pearlRng = new Random(seed);
        }
        this.markDirty();
        return this.pearlRng;
    }

    public void setPearlRng(Random rng) {
        this.pearlRng = rng;
        this.markDirty();
    }
}