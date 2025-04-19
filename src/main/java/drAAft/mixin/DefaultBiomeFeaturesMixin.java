package drAAft.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.CountDepthDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DefaultBiomeFeatures.class)
public class DefaultBiomeFeaturesMixin {
    /**
     * @author pacmanmvc
     * @reason more debris
     */
    @Overwrite
    public static void addAncientDebris(Biome biome) {
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                Feature.NO_SURFACE_ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.getDefaultState(), 5))
                        .createDecoratedFeature(Decorator.COUNT_DEPTH_AVERAGE.configure(new CountDepthDecoratorConfig(1, 16, 8)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                Feature.NO_SURFACE_ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.getDefaultState(), 4))
                        .createDecoratedFeature(Decorator.COUNT_DEPTH_AVERAGE.configure(new CountDepthDecoratorConfig(1, 16, 8)))
        );
    }
}
