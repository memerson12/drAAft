package drAAft.mixin;

import net.minecraft.world.biome.BambooJungleBiome;
import net.minecraft.world.biome.BambooJungleHillsBiome;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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
