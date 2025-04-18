package aaeventmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.gen.PhantomSpawner;
import net.minecraft.world.gen.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;

/**
 * Mixin to modify Phantom spawning behavior.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin implements Spawner {

    @Shadow private int ticksUntilNextSpawn;

    /**
     * Overwrites the vanilla phantom spawning logic to customize spawn delay,
     * cooldown, difficulty check, and minimum spawn count on hard difficulty.
     *
     * @author Memerson (and gemini)
     * @reason Apply multiple custom changes to phantom spawning mechanics.
     *
     * @param serverWorld The world where spawning occurs.
     * @param spawnMonsters Whether monster spawning is enabled.
     * @param spawnAnimals Whether animal spawning is enabled (unused by phantoms but part of signature).
     * @return The number of phantoms spawned in this tick.
     */
    @Overwrite
    public int spawn(ServerWorld serverWorld, boolean spawnMonsters, boolean spawnAnimals) {
        // Check if monster spawning is enabled globally for the server
        if (!spawnMonsters) {
            return 0;
        }
        // Check the DO_INSOMNIA game rule
        if (!serverWorld.getGameRules().getBoolean(GameRules.DO_INSOMNIA)) {
            return 0;
        }

        Random random = serverWorld.random;
        // Decrease cooldown timer
        this.ticksUntilNextSpawn--;

        // If cooldown is still active, do nothing
        if (this.ticksUntilNextSpawn > 0) {
            return 0;
        }

        // --- MODIFICATION: Cooldown changed to 15-60 seconds (300-1180 ticks) ---
        // Reset cooldown timer with the new range
        // Original: this.ticksUntilNextSpawn += (60 + random.nextInt(60)) * 20; // 1200-2380 ticks
        this.ticksUntilNextSpawn = (15 + random.nextInt(45)) * 20;

        // Check world conditions (light level, dimension sky light) - Unchanged from vanilla
        if (serverWorld.getAmbientDarkness() < 5 && serverWorld.getDimension().hasSkyLight()) {
            return 0;
        }

        int totalSpawnedThisTick = 0;
        // Iterate through players to check spawn conditions near them
        for (PlayerEntity playerEntity : serverWorld.getPlayers()) {
            // Skip spectators
            if (playerEntity.isSpectator()) {
                continue;
            }

            BlockPos playerPos = playerEntity.getBlockPos();

            // Check dimension/sky conditions near the player - Unchanged from vanilla
            if (!serverWorld.getDimension().hasSkyLight() || (playerPos.getY() >= serverWorld.getSeaLevel() && serverWorld.isSkyVisible(playerPos))) {

                LocalDifficulty localDifficulty = serverWorld.getLocalDifficulty(playerPos);

                // --- MODIFICATION: Difficulty check multiplier removed ---
                // Check if local difficulty is high enough based on a random roll
                // Original: if (localDifficulty.isHarderThan(random.nextFloat() * 3.0F))
                if (localDifficulty.isHarderThan(random.nextFloat())) { // Removed 3.0F multiplier

                    // Check player stats (time since last rest)
                    ServerStatHandler serverStatHandler = ((ServerPlayerEntity) playerEntity).getStatHandler();
                    // Clamp timeSinceRest to avoid issues, minimum 1 tick.
                    int timeSinceRest = MathHelper.clamp(serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);

                    // --- MODIFICATION: Minimum time since rest reduced ---
                    // Check if the player has been awake long enough (random chance based on time)
                    // Original: if (random.nextInt(timeSinceRest) >= 72000) // 1 hour
                    if (random.nextInt(timeSinceRest) >= 12_000) {

                        // Calculate potential spawn position relative to the player
                        BlockPos spawnPos = playerPos.up(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
                        BlockState blockState = serverWorld.getBlockState(spawnPos);
                        FluidState fluidState = serverWorld.getFluidState(spawnPos);

                        // Check if the calculated position is suitable for phantom spawning
                        if (SpawnHelper.isClearForSpawn(serverWorld, spawnPos, blockState, fluidState, EntityType.PHANTOM)) {
                            EntityData entityData = null; // Data for initializing the entity group

                            // Calculate number of phantoms to spawn in this group based on global difficulty
                            int groupSize = 1 + random.nextInt(localDifficulty.getGlobalDifficulty().getId() + 1);

                            // --- MODIFICATION: Ensure minimum 2 spawns on HARD ---
                            // If difficulty is HARD and calculated size is less than 2, set it to 2.
                            if (localDifficulty.getGlobalDifficulty() == Difficulty.HARD && groupSize < 2) {
                                groupSize = 2;
                            }

                            // Spawn the group of phantoms
                            for(int i = 0; i < groupSize; ++i) {
                                PhantomEntity phantomEntity = EntityType.PHANTOM.create(serverWorld);
                                // Basic null check for safety, though create() should ideally not return null here
                                if (phantomEntity != null) {
                                    phantomEntity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
                                    // Initialize the phantom (e.g., sets NBT data)
                                    entityData = phantomEntity.initialize(serverWorld, localDifficulty, SpawnReason.NATURAL, entityData, null);
                                    // Add the phantom to the world
                                    serverWorld.spawnEntity(phantomEntity);
                                }
                            }
                            // Add the number spawned in this group to the total for this tick
                            totalSpawnedThisTick += groupSize;
                        }
                    }
                }
            }
        }
        // Return the total number of phantoms spawned across all player checks this tick
        return totalSpawnedThisTick;
    }
}
