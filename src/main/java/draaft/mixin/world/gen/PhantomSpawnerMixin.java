package draaft.mixin.world.gen;

import draaft.persistent.WorldState;
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

import java.util.Random;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin implements Spawner {
    @Shadow private int ticksUntilNextSpawn;

    /**
     * Overwrites the vanilla phantom spawning logic to change minimum spawn delay,
     * difficulty check, and minimum spawn count on hard difficulty.
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
        if (!spawnMonsters) {
            return 0;
        }
        if (!serverWorld.getGameRules().getBoolean(GameRules.DO_INSOMNIA)) {
            return 0;
        }
        WorldState state = WorldState.getServerState(serverWorld);
        Random draaftPhantomRng = state.getOrCreatePhantomRng(serverWorld);
        this.ticksUntilNextSpawn--;

        if (this.ticksUntilNextSpawn > 0) {
            return 0;
        }
        this.ticksUntilNextSpawn = this.ticksUntilNextSpawn + (60 + draaftPhantomRng.nextInt(60)) * 20;

        if (serverWorld.getAmbientDarkness() < 5 && serverWorld.getDimension().hasSkyLight()) {
            return 0;
        }
        int totalSpawnedThisTick = 0;

        for (PlayerEntity playerEntity : serverWorld.getPlayers()) {
            if (playerEntity.isSpectator()) {
                continue;
            }
            BlockPos playerPos = playerEntity.getBlockPos();

            if (!serverWorld.getDimension().hasSkyLight() || (playerPos.getY() >= serverWorld.getSeaLevel() && serverWorld.isSkyVisible(playerPos))) {
                LocalDifficulty localDifficulty = serverWorld.getLocalDifficulty(playerPos);

                if (localDifficulty.isHarderThan(draaftPhantomRng.nextFloat())) {
                    ServerStatHandler serverStatHandler = ((ServerPlayerEntity) playerEntity).getStatHandler();
                    int timeSinceRest = MathHelper.clamp(serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);

                    if (draaftPhantomRng.nextInt(timeSinceRest) >= 12_000) {
                        BlockPos spawnPos = playerPos.up(20 + draaftPhantomRng.nextInt(15)).east(-10 + draaftPhantomRng.nextInt(21)).south(-10 + draaftPhantomRng.nextInt(21));
                        BlockState blockState = serverWorld.getBlockState(spawnPos);
                        FluidState fluidState = serverWorld.getFluidState(spawnPos);

                        if (SpawnHelper.isClearForSpawn(serverWorld, spawnPos, blockState, fluidState, EntityType.PHANTOM)) {
                            EntityData entityData = null;
                            int groupSize = 1 + draaftPhantomRng.nextInt(localDifficulty.getGlobalDifficulty().getId() + 1);

                            if (localDifficulty.getGlobalDifficulty() == Difficulty.HARD && groupSize < 2) {
                                groupSize = 2;
                            }
                            for(int i = 0; i < groupSize; ++i) {
                                PhantomEntity phantomEntity = EntityType.PHANTOM.create(serverWorld);

                                if (phantomEntity != null) {
                                    phantomEntity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
                                    entityData = phantomEntity.initialize(serverWorld, localDifficulty, SpawnReason.NATURAL, entityData, null);
                                    serverWorld.spawnEntity(phantomEntity);
                                }
                            }
                            totalSpawnedThisTick += groupSize;
                        }
                    }
                }
            }
        }

        return totalSpawnedThisTick;
    }
}
