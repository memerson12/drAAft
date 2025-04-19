package drAAft.mixin;

import net.minecraft.world.biome.BambooJungleBiome;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BambooJungleBiome.class)
public abstract class BambooJungleBiomeMixin extends Biome {
    protected BambooJungleBiomeMixin(Settings settings) {
        super(settings);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 80))
    private int injected(int value) {
        return 8_000;
    }
}
