package draaft.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.StrafePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(StrafePlayerPhase.class)
public abstract class StrafePlayerPhaseMixin extends AbstractPhase {
    public StrafePlayerPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Redirect(method = "method_6861", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float injected(Random instance) {
        return 0.0F;
    }
}
