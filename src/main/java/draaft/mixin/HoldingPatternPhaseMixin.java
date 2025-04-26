package draaft.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseMixin extends AbstractPhase {
    public HoldingPatternPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @ModifyConstant(method = "method_6842", constant = @Constant(floatValue = 20.0F))
    private float injectedFloat(float value) {
        return 10.0F;
    }

    @Redirect(method = "method_6841", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int injectedInt(Random instance, int i) {
        instance.setSeed(getBlockSeed(instance, this.dragon.getEntityWorld().getServer().getSaveProperties().getGeneratorOptions().getSeed(), this.dragon.getBlockPos().getX(), this.dragon.getBlockPos().getZ()));
        return instance.nextInt(i);
    }

    @Unique
    private static long getBlockSeed(Random random, long seed, int x, int z) {
        random.setSeed(seed);
        long l = random.nextLong() | 1L;
        long m = random.nextLong() | 1L;
        return (long) x * l + (long) z * m ^ seed;
    }
}
