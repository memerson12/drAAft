package draaft.mixin.server.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements ServerWorldAccess {
    @Shadow @Final private ServerWorldProperties worldProperties;

    @Shadow @Final private MinecraftServer server;

    @Unique
    boolean lastTickThunder = true;
    @Unique
    boolean lastTickRain = true;

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
}