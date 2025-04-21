package draaft.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseMixin extends AbstractPhase {
    public HoldingPatternPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Redirect(method = "method_6842", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float injectedFloat(Random instance) {
        return 0.0F;
    }

    @Redirect(method = "method_6841", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int injectedInt(Random instance, int i) {
        instance.setSeed(this.dragon.getEntityWorld().getServer().getSaveProperties().getGeneratorOptions().getSeed());
        return instance.nextInt(i);
    }
}
