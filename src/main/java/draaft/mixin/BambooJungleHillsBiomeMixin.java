package draaft.mixin;

import net.minecraft.world.biome.BambooJungleHillsBiome;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BambooJungleHillsBiome.class)
public abstract class BambooJungleHillsBiomeMixin extends Biome {
    protected BambooJungleHillsBiomeMixin(Settings settings) {
        super(settings);
    }

    @Override
    public float getMaxSpawnChance() {
        return 0.3F;
    }
}
