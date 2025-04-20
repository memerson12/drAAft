package draaft.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements ServerWorldAccess {
    @Shadow @Final private ServerWorldProperties worldProperties;

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setThunderTime(I)V"))
    private int injectedThunder(int thunderTime) {
        if (this.worldProperties.isThundering()) {
            return thunderTime;
        }
        Random random = new Random(thunderTime);
        return thunderTime > 36000 ? random.nextInt(24000) + 12000 : thunderTime;
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setRainTime(I)V"))
    private int injectedRain(int rainTime) {
        if (this.worldProperties.isRaining()) {
            return rainTime;
        }
        int thunderTime = this.worldProperties.getThunderTime();
        Random random = new Random(rainTime);
        return rainTime > 36000 ? (thunderTime - (random.nextInt(10800) + 1200)) : rainTime;
    }
}
