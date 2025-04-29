package draaft.mixin;

import draaft.api.EnderDragonEntityAccessor;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseMixin extends AbstractPhase {
    public HoldingPatternPhaseMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Unique
    private Random random;

    @Inject(method = "beginPhase", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (this.dragon instanceof EnderDragonEntityAccessor) {
            EnderDragonEntityAccessor dragonAccessor = (EnderDragonEntityAccessor) this.dragon;
            this.random = dragonAccessor.draaft$getRandom();
        }
    }

    @ModifyConstant(method = "method_6842", constant = @Constant(floatValue = 20.0F))
    private float injectedFloat(float value) {
        return 10.0F;
    }

    @Redirect(method = "method_6841", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int injectedInt(Random instance, int i) {
        return this.random.nextInt(i);
    }
}
