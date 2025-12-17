package draaft.mixin.world.biome.source;

import com.google.common.collect.Lists;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {
    @Inject(method = "getSpawnBiomes", at = @At("HEAD"), cancellable = true)
    void getSpawnBiomes(CallbackInfoReturnable<List<Biome>> cir) {
        cir.setReturnValue(Lists.newArrayList(
            Biomes.FOREST, Biomes.DESERT, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS
        ));
    }
}
