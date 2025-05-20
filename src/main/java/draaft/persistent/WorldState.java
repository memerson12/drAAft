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
import java.util.EnumMap; // Use EnumMap for better performance with enum keys
import java.util.Map;
import java.util.Random;

public class WorldState extends PersistentState {

    /**
     * Enum defining the different types of Random Number Generators managed by WorldState.
     */
    public enum RngType {
        PEARL("pearl"),
        BARTER("barter"),
        TRIDENT("trident"),
        SKULL("skull"),
        CAT("cat"),
        PHANTOM("phantom"),
        BLAZE("blaze"),
        SHULKER("shulker"),
        RABBIT("rabbit");

        private final String keyName; // The base name used for NBT keys

        RngType(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getNbtKey() {
            return keyName + "_rng";
        }

        public String getFallbackNbtKey() {
            return keyName + "_seed";
        }
    }

    // Use EnumMap for potentially better performance and memory usage with enum keys
    private final Map<RngType, RandomState> randomStates = new EnumMap<>(RngType.class);

    public WorldState(String key) {
        super(key);
        for (RngType type : RngType.values()) {
            randomStates.put(type, new RandomState(null, type));
        }
    }

    /**
     * Gets or creates the persistent WorldState for the given server world.
     * @param world The server world.
     * @return The WorldState instance.
     */
    public static WorldState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                () -> new WorldState("draaft_world_state"),
                "draaft_world_state");
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for (RandomState randomState : randomStates.values()) {
            deserializeFromTag(tag, randomState);
        }
    }

    private void deserializeFromTag(CompoundTag tag, RandomState randomState) {
        String primaryKey = randomState.getNbtKey();
        if (tag.contains(primaryKey)) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(tag.getByteArray(primaryKey));
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                randomState.setRandom((Random) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                // If deserialization fails, attempt to fall back to using the stored seed
                draaft.LOGGER.warn("Unable to deserialize RNG state for key '{}', attempting fallback.", primaryKey, e);
                tryFallbackSeed(tag, randomState);
            }
        } else {
            // If primary key doesn't exist, try the fallback seed key directly
            tryFallbackSeed(tag, randomState);
        }
    }

    private void tryFallbackSeed(CompoundTag tag, RandomState randomState) {
        String fallbackKey = randomState.getFallbackNbtKey();
        if (tag.contains(fallbackKey)) {
            draaft.LOGGER.info("Falling back to seed for RNG state key '{}'", fallbackKey);
            randomState.setRandom(new Random(tag.getLong(fallbackKey)));
        } else {
            // Neither key found; RNG will be initialized lazily if requested via getOrCreateRng
            draaft.LOGGER.debug("Neither primary key '{}' nor fallback key '{}' found for RNG type {}.",
                    randomState.getNbtKey(), fallbackKey, randomState.getType().name());
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        for (RandomState randomState : randomStates.values()) {
            serializeToTag(nbt, randomState);
        }
        return nbt;
    }

    private void serializeToTag(CompoundTag nbt, RandomState randomState) {
        if (randomState.getRandom() != null) {
            String primaryKey = randomState.getNbtKey();
            String fallbackKey = randomState.getFallbackNbtKey();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(randomState.getRandom());
                nbt.putByteArray(primaryKey, baos.toByteArray());
                // Clean up the old fallback key if full serialization is successful
                nbt.remove(fallbackKey);
            } catch (IOException e) {
                // If full serialization fails, fall back to storing only the seed
                draaft.LOGGER.warn("Unable to serialize RNG state for key '{}', falling back to storing seed.", primaryKey, e);
                nbt.putLong(fallbackKey, randomState.getRandom().nextLong());
                // Clean up the primary key if fallback seed is used
                nbt.remove(primaryKey);
            }
        }
    }

    /**
     * Gets the Random instance for the specified type, creating it based on the
     * world seed if it doesn't exist yet in this session.
     *
     * @param type  The RngType enum constant representing the desired RNG.
     * @param world The ServerWorld instance.
     * @return The Random instance for the specified type.
     */
    public Random getOrCreateRng(RngType type, ServerWorld world) {
        // EnumMap guarantees the key exists if initialized correctly
        RandomState randomState = randomStates.get(type);

        if (randomState.getRandom() == null) {
            draaft.LOGGER.info("Initializing '{}' RNG state. Is Client: {}", type.name(), world.isClient);
            long seed = world.getSeed();
            randomState.setRandom(new Random(seed));
        }

        this.markDirty();
        return randomState.getRandom();
    }

    private static class RandomState {
        private Random random;
        private final RngType type;

        public RandomState(Random random, RngType type) {
            this.random = random;
            this.type = type;
        }

        public Random getRandom() {
            return random;
        }

        public void setRandom(Random random) {
            this.random = random;
        }

        public String getNbtKey() {
            return type.getNbtKey();
        }

        public String getFallbackNbtKey() {
            return type.getFallbackNbtKey();
        }

        public RngType getType() {
            return type;
        }
    }
}