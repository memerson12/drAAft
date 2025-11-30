package draaft.mixin.client;

import draaft.draaft;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    void onKey(long window, int key, int scancode, int i, int modifiers, CallbackInfo ci) {
        if (draaft.NOF3 && key == 292) {
            ci.cancel();
        }
    }

    @Inject(method = "processF3", at = @At("HEAD"), cancellable = true)
    void processF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (draaft.NOF3 && (key == 67 || key == 73)) {
            cir.setReturnValue(false);
        }
    }
}
