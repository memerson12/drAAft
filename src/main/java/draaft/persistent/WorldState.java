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
    private final RandomState pearlRng = new RandomState(null, "pearl_rng", "pearl_seed");
    private final RandomState barterRng = new RandomState(null, "barter_rng", "barter_seed");
    private final RandomState tridentRng = new RandomState(null, "trident_rng", "trident_seed");
    private final RandomState skullRng = new RandomState(null, "skull_rng", "skull_seed");
    private final RandomState catRng = new RandomState(null, "cat_rng", "cat_seed");
    private final RandomState phantomRng = new RandomState(null, "phantom_rng", "phantom_seed");
    private final RandomState blazeRng = new RandomState(null, "blaze_rng", "blaze_seed");
    private final RandomState shulkerRng = new RandomState(null, "shulker_rng", "shulker_seed");

    public WorldState(String key) {
        super(key);
    }

    public static WorldState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new WorldState("draaft_world_state"),
                "draaft_world_state");
    }

    @Override
    public void fromTag(CompoundTag tag) {
        serializeRandom(tag, pearlRng);
        serializeRandom(tag, barterRng);
        serializeRandom(tag, tridentRng);
        serializeRandom(tag, skullRng);
        serializeRandom(tag, catRng);
        serializeRandom(tag, phantomRng);
        serializeRandom(tag, blazeRng);
        serializeRandom(tag, shulkerRng);
    }

    private void serializeRandom(CompoundTag tag, RandomState randomState) {
        if (tag.contains(randomState.getKey())) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(tag.getByteArray(randomState.getKey()));
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                randomState.setRandom((Random) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                // If deserialization fails, fall back to just using the seed
                if (tag.contains(randomState.getFallbackKey())) {
                    draaft.LOGGER.warn("Unable to deserialize {}", randomState.getKey());
                    randomState.setRandom(new Random(tag.getLong(randomState.getFallbackKey())));
                }
            }
        } else if (tag.contains(randomState.getFallbackKey())) {
            randomState.setRandom(new Random(tag.getLong(randomState.getFallbackKey())));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        deserializeRandom(nbt, pearlRng);
        deserializeRandom(nbt, barterRng);
        deserializeRandom(nbt, tridentRng);
        deserializeRandom(nbt, skullRng);
        deserializeRandom(nbt, catRng);
        deserializeRandom(nbt, phantomRng);
        deserializeRandom(nbt, blazeRng);
        deserializeRandom(nbt, shulkerRng);

        return nbt;
    }

    private void deserializeRandom(CompoundTag nbt, RandomState randomState) {
        if (randomState.getRandom() != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(randomState.getRandom());
                nbt.putByteArray(randomState.getKey(), baos.toByteArray());
            } catch (IOException e) {
                // If serialization fails, fall back to just storing the seed
                draaft.LOGGER.warn("Unable to serialize {}", randomState.getFallbackKey());
                nbt.putLong(randomState.getFallbackKey(), randomState.getRandom().nextLong());
            }
        }
    }

    public Random getOrCreatePearlRng(ServerWorld world) {
        if (this.pearlRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.pearlRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.pearlRng.getRandom();
    }

    public Random getOrCreateBarterRng(ServerWorld world) {
        if (this.barterRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.barterRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.barterRng.getRandom();
    }

    public Random getOrCreateTridentRng(ServerWorld world) {
        if (this.tridentRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.tridentRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.tridentRng.getRandom();
    }

    public Random getOrCreateSkullRng(ServerWorld world) {
        if (this.skullRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.skullRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.skullRng.getRandom();
    }

    public Random getOrCreateCatRng(ServerWorld world) {
        if (this.catRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.catRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.catRng.getRandom();
    }

    public Random getOrCreatePhantomRng(ServerWorld world) {
        if (this.phantomRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.phantomRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.phantomRng.getRandom();
    }

    public Random getOrCreateBlazeRng(ServerWorld world) {
        if (this.blazeRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.blazeRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.blazeRng.getRandom();
    }

    public Random getOrCreateShulkerRng(ServerWorld world) {
        if (this.shulkerRng.getRandom() == null) {
            draaft.LOGGER.info("Is Client: {}", world.isClient);
            long seed = world.getSeed();
            this.shulkerRng.setRandom(new Random(seed));
        }
        this.markDirty();
        return this.shulkerRng.getRandom();
    }

    private static class RandomState {
        private Random random;
        private final String key;
        private final String fallbackKey;

        public RandomState(Random random, String key, String fallbackKey) {
            this.random = random;
            this.key = key;
            this.fallbackKey = fallbackKey;
        }

        public Random getRandom() {
            return random;
        }

        public String getKey() {
            return key;
        }

        public String getFallbackKey() {
            return fallbackKey;
        }

        public void setRandom(Random random) {
            this.random = random;
        }
    }
}