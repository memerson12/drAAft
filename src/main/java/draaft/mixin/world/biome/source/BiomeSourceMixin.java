package draaft.mixin.world.biome.source;

import com.google.common.collect.Lists;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {
    @Shadow
    @Final
    private static List<Biome> SPAWN_BIOMES = Lists.<Biome>newArrayList(
        Biomes.FOREST, Biomes.DESERT, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS
    );
}
