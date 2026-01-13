package draaft.mixin.server.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import draaft.mixin.world.gen.chunk.ChunkGeneratorAccessor;
import draaft.persistent.WorldManifest;
import draaft.persistent.WorldState;
import draaft.world.ServerWorldInterface;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements ServerWorldAccess, ServerWorldInterface {
    @Shadow
    @Final
    private ServerWorldProperties worldProperties;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Unique
    boolean lastTickThunder = true;
    @Unique
    boolean lastTickRain = true;

    @Unique
    private @Nullable WorldManifest worldManifest = null;

    @Unique
    private boolean isIllegalRain(int timer) {
        return timer > 1_000_000;
    }

    @Unique
    private int getRainFromThunder(int thunder) {
        Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());
        return thunder - (random.nextInt(7200) + 1200);
    }

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }

    @ModifyArg(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setThunderTime(I)V"))
    private int injectedThunder(int thunderTime) {


        /*
        TODO: BLACK MAGIC

        if it just started not thundering, either because thunder ended OR we made the world
        then make thunder time a seeded value 10-45 minutes
        if it has been not thundering
        then decrement by 1
        if it just started thundering
        then make thunder time a seeded value 3-13 minutes
        if it has been thundering
        then decrement by 1

        for simplicity:
        if current state is same as previous state
        then decrement by 1
         */
        if ((lastTickThunder && this.worldProperties.getThunderTime() == 0 && !this.worldProperties.isThundering()) || this.worldProperties.getTime() == 0) {
            Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());
            lastTickThunder = false;
            int newThunder = random.nextInt(42000) + 12000;

            // right now, it is guaranteed to not be thundering.
            // check if we have to reset our rain timer based on this newly calculated thunder timer
            if (this.isIllegalRain(this.worldProperties.getRainTime())) {
                this.worldProperties.setRainTime(this.getRainFromThunder(newThunder));
            }

            return newThunder;
        } else if (!lastTickThunder && this.worldProperties.getThunderTime() == 0 && this.worldProperties.isThundering()) {
            Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());
            lastTickThunder = true;
            return random.nextInt(12000) + 3600;
        }

        return thunderTime;
    }

    @ModifyArg(method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setRainTime(I)V"))
    private int injectedRain(int rainTime) {
        // if we are waiting for a 'valid' rain time, simply chillax. it'll come when it comes.
        if (this.isIllegalRain(rainTime)) {
            // keep current time. no decrementing! shame on you, really, for trying to subtract from such a majestic number.
            return this.worldProperties.getRainTime();
        }

        /*
        TODO: BLACK MAGIC

        if it just started not raining, either because rain ended OR we made the world
        then make rain time a seeded value 1-7 minutes less than thunder time
        if it has been not raining
        then decrement by 1
        if it just started raining
        then make rain time a seeded value 10-20 minutes
        if it has been raining
        then decrement by 1

        for simplicity:
        if current state is same as previous state
        then decrement by 1
         */
        if ((lastTickRain && this.worldProperties.getRainTime() == 0 && !this.worldProperties.isRaining()) || this.worldProperties.getTime() == 0) {
            lastTickRain = false;

            if (this.worldProperties.isThundering()) {
                // currently thundering -- we cannot actually set our rain timer :/ sad!
                // instead, set it to a sentinel value and wait for it to stop thundering.
                return 2_000_000;
            }

            int thunderTime = this.worldProperties.getThunderTime();
            return this.getRainFromThunder(thunderTime);
        } else if (!lastTickRain && this.worldProperties.getRainTime() == 0 && this.worldProperties.isRaining()) {
            Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());
            lastTickRain = true;
            return random.nextInt(12000) + 12000;
        }

        return rainTime;
    }

    @Inject(method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;", at = @At("HEAD"))
    private void createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, CallbackInfoReturnable<Explosion> cir) {
        final float DEBRIS_CHANCE = WorldManifest.get((ServerWorld) this.getWorld()).on(WorldManifest.Feature.DEBRIS_RATES) ? 1F : 0.1F;

        if (this.getDimension().isUltrawarm() && (y >= 5 && y <= 25)) {
            ServerWorld world = (ServerWorld) this.getWorld();
            WorldState state = WorldState.getServerState(world);
            WorldState.RandomState draaftTntState = state.getOrCreateRng(WorldState.RngType.NETHERITE_EXPLOSION, world);
            int tnt = draaftTntState.incrementUses();
            float chance = Math.abs(y - 15) < 3 ? DEBRIS_CHANCE : DEBRIS_CHANCE / 2; // y13-17 10% otherwise 5%, to incentivize mining at correct y-height
            float timer = 1 / chance;

            if (draaftTntState.getRandom().nextFloat() < chance || (tnt % timer) == 0) {
                placeDebrisBlob(draaftTntState.getRandom(), new BlockPos(x, y, z), 10);
                draaftTntState.resetUses();
            }
        }
    }

    @Unique
    private void placeDebrisBlob(Random random, BlockPos pos, int depth) {
        if (depth <= 0) {
            return;
        }
        int count = 2 + random.nextInt(2);
        int x = 0, y = 0, z = 0;

        while ((x == 0 && z == 0) && (y == 0 || y == 1)) {
            x = random.nextInt(6) - random.nextInt(6);
            y = random.nextInt(6) - random.nextInt(6);
            z = random.nextInt(6) - random.nextInt(6);
        }
        BlockPos start = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
        if (placeDebrisBlock(start)) {
            count--;
            int fails = 0;

            while (count > 0 && fails < 10) {
                Direction direction = Direction.random(random);
                start = start.offset(direction);
                if (placeDebrisBlock(start)) {
                    count--;
                } else {
                    fails++;
                }
            }
        } else {
            placeDebrisBlob(random, pos, depth - 1);
        }
    }

    @Unique
    private boolean placeDebrisBlock(BlockPos pos) {
        if (this.getWorld().getBlockState(pos).equals(Blocks.ANCIENT_DEBRIS.getDefaultState())) {
            return false;
        }
        if (!this.getWorld().getBlockState(pos).isAir() && this.getWorld().getBlockState(pos).getFluidState().isEmpty()) {
            this.getWorld().setBlockState(pos, Blocks.ANCIENT_DEBRIS.getDefaultState());
            return true;
        }
        return false;
    }

    @Override
    public WorldManifest draaft$getWorldManifest() {
        return this.worldManifest;
    }

    @Override
    public void draaft$setWorldManifest(WorldManifest worldManifest) {
        this.worldManifest = worldManifest;
    }

    /**
     * @author me_nx
     * @reason dimension split seeds
     */
    @Overwrite
    public long getSeed() {
        return ((ChunkGeneratorAccessor) this.getChunkManager().getChunkGenerator()).draaft$seed();
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static long injectSeed(long overworldSeed, @Local(argsOnly = true) ChunkGenerator chunkGenerator) {
        return ((ChunkGeneratorAccessor) chunkGenerator).draaft$seed();
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonFight;"))
    private EnderDragonFight injectDragonFightSeed(ServerWorld world, long _s, CompoundTag tag, Operation<EnderDragonFight> op) {
        return op.call(world, world.getSeed(), tag);
    }
}
