package draaft.persistent;

import draaft.draaft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.io.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class WorldState extends PersistentState {

    /**
     * Enum defining the different types of Random Number Generators managed by WorldState.
     */
    public enum RngType {
        TEMPLE("temple", World.OVERWORLD),
        DEAD_BUSH("dead_bush", World.OVERWORLD),
        LAPIS("lapis", World.OVERWORLD),
        PEARL("pearl", World.OVERWORLD),
        TRIDENT("trident", World.OVERWORLD),
        CAT("cat", World.OVERWORLD),
        PHANTOM("phantom", World.OVERWORLD),
        RABBIT("rabbit", World.OVERWORLD),
        JUNK("junk", World.OVERWORLD),
        EXPLODING_SHELLS("exploding_shells", World.OVERWORLD),
        BLAZE("blaze", World.NETHER),
        BARTER("barter", World.NETHER),
        SKULL("skull", World.NETHER),
        NETHERITE_TNT("netherite_tnt", World.NETHER),
        NETHERITE_MINED("netherite_mined", World.NETHER),
        SHULKER("shulker", World.END);

        private final String keyName; // The base name used for NBT keys
        private final RegistryKey<World> seedSourceWorld;

        RngType(String keyName, RegistryKey<World> seedSourceWorld) {
            this.keyName = keyName;
            this.seedSourceWorld = seedSourceWorld;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getNbtKey() {
            return keyName + "_rng";
        }

        public String getCounterNbtKey() {
            return keyName + "_uses";
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
     *
     * @param world The server world.
     * @return The WorldState instance.
     */
    public static WorldState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new WorldState("draaft_world_state"), "draaft_world_state");
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for (RandomState randomState : randomStates.values()) {
            deserializeFromTag(tag, randomState);
        }
    }

    private void deserializeFromTag(CompoundTag tag, RandomState randomState) {
        String primaryKey = randomState.getNbtKey();
        String counterKey = randomState.getCounterNbtKey();
        if (tag.contains(primaryKey)) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(tag.getByteArray(primaryKey)); ObjectInputStream ois = new ObjectInputStream(bais)) {
                randomState.setRandom((Random) ois.readObject());
                randomState.setUses(tag.getInt(counterKey));
            } catch (IOException | ClassNotFoundException e) {
                draaft.LOGGER.warn("Unable to deserialize RNG state for key '{}', will create new random.", primaryKey, e);
            }
        } else {
            draaft.LOGGER.warn("No existing RNG state found for key '{}', will create new random.", primaryKey);
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
            String counterKey = randomState.getCounterNbtKey();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(randomState.getRandom());
                nbt.putByteArray(primaryKey, baos.toByteArray());
                nbt.putInt(counterKey, randomState.getUses());
            } catch (IOException e) {
                draaft.LOGGER.warn("Unable to serialize RNG state for key '{}'", primaryKey, e);
            }
        }
    }

    /**
     * Gets the RandomState instance for the specified type, creating it based on the
     * world seed if it doesn't exist yet in this session.
     *
     * @param type  The RngType enum constant representing the desired RNG.
     * @param world The ServerWorld instance.
     * @return The RandomState instance for the specified type.
     */
    public RandomState getOrCreateRng(RngType type, ServerWorld world) {
        // EnumMap guarantees the key exists if initialized correctly
        RandomState randomState = randomStates.get(type);

        if (randomState.getRandom() == null) {
            draaft.LOGGER.info("Initializing '{}' RNG state.", type.name());
            long seed = Objects.requireNonNull(world.getServer().getWorld(type.seedSourceWorld)).getSeed();
            randomState.setRandom(new Random(seed));
        }

        this.markDirty();
        return randomState;
    }

    public static class RandomState {
        private Random random;
        private final RngType type;
        private int uses;

        public RandomState(Random random, RngType type) {
            this.random = random;
            this.type = type;
            this.uses = 0;
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

        public String getCounterNbtKey() {
            return type.getCounterNbtKey();
        }

        public RngType getType() {
            return type;
        }

        public int getUses() {
            return uses;
        }

        public void setUses(int uses) {
            this.uses = uses;
        }

        public int incrementUses() {
            return ++this.uses;
        }

        public void resetUses() {
            this.uses = 0;
        }
    }
}
