package draaft.mixin.compat;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameTimer.class)
public class InGameTimerMixin {
    @Inject(method = "tryInsertNewAdvancement", at = @At("HEAD"), cancellable = true, remap = false)
    private void redirectTryInsertNewAdvancement(String advancementID, String criteriaKey, boolean isAdvancement, CallbackInfo ci) {
        if (!advancementID.startsWith("minecraft")) {
            ci.cancel();
        }
    }

    @Inject(method = "getMoreData", at = @At("HEAD"), remap = false, cancellable = true)
    private void modifyAdvancementData(int key, CallbackInfoReturnable<Integer> cir) {
        if (key == 7441) {
            cir.setReturnValue(80);
        }
    }
}
