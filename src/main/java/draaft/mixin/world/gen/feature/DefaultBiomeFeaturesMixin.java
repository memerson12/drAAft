package draaft.mixin.world.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.CountDepthDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DefaultBiomeFeatures.class)
public class DefaultBiomeFeaturesMixin {
    @Unique
    private static final BlockState COAL_ORE = Blocks.COAL_ORE.getDefaultState();
    @Unique
    private static final BlockState IRON_ORE = Blocks.IRON_ORE.getDefaultState();
    @Unique
    private static final BlockState GOLD_ORE = Blocks.GOLD_ORE.getDefaultState();
    @Unique
    private static final BlockState REDSTONE_ORE = Blocks.REDSTONE_ORE.getDefaultState();
    @Unique
    private static final BlockState DIAMOND_ORE = Blocks.DIAMOND_ORE.getDefaultState();
    @Unique
    private static final BlockState LAPIS_ORE = Blocks.LAPIS_ORE.getDefaultState();

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

    /**
     * @author pacmanmvc
     * @reason more lapis
     */
    @Overwrite
    public static void addDefaultOres(Biome biome) {
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, COAL_ORE, 17))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(20, 0, 0, 128)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, IRON_ORE, 9))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(20, 0, 0, 64)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, GOLD_ORE, 9))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(2, 0, 0, 32)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, REDSTONE_ORE, 8))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(8, 0, 0, 16)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, DIAMOND_ORE, 8))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(1, 0, 0, 16)))
        );
        biome.addFeature(
                GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, LAPIS_ORE, 9))
                        .createDecoratedFeature(Decorator.COUNT_DEPTH_AVERAGE.configure(new CountDepthDecoratorConfig(4, 16, 16)))
        );
    }
}
