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
    boolean injectedThunder = false;
    @Unique
    boolean injectedRain = false;

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setThunderTime(I)V"))
    private int injectedThunder(int thunderTime) {
        if (this.worldProperties.isThundering()) {
            return thunderTime;
        }
        Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed());
        if (!injectedThunder) {
            injectedThunder = true;
            return random.nextInt(42000) + 12000;
        }
        return thunderTime > 54000 ? random.nextInt(42000) + 12000 : thunderTime;
    }

    @ModifyArg(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;setRainTime(I)V"))
    private int injectedRain(int rainTime) {
        if (this.worldProperties.isRaining()) {
            return rainTime;
        }
        Random random = new Random(this.server.getSaveProperties().getGeneratorOptions().getSeed());
        int thunderTime = this.worldProperties.getThunderTime();
        if (!injectedRain) {
            injectedRain = true;
            return (thunderTime - (random.nextInt(7200) + 1200));
        }
        return rainTime > 54000 ? (thunderTime - (random.nextInt(7200) + 1200)) : rainTime;
    }
}
