package draaft.mixin;

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

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setThunderTime(I)V"))
    private int injectedThunder(int thunderTime) {
        Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());

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
            lastTickThunder = false;
            return random.nextInt(42000) + 12000;
        } else if (!lastTickThunder && this.worldProperties.getThunderTime() == 0 && this.worldProperties.isThundering()) {
            lastTickThunder = true;
            return random.nextInt(12000) + 3600;
        }

        return thunderTime;
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setRainTime(I)V"))
    private int injectedRain(int rainTime) {
        Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed() + this.worldProperties.getTimeOfDay());

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
            int thunderTime = this.worldProperties.getThunderTime();
            return thunderTime - (random.nextInt(7200) + 1200);
        } else if (!lastTickRain && this.worldProperties.getRainTime() == 0 && this.worldProperties.isRaining()) {
            lastTickRain = true;
            return random.nextInt(12000) + 12000;
        }

        return rainTime;
    }
}
