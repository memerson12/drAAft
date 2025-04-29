package draaft.mixin;

import net.minecraft.world.gen.decorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.decorator.TreeDecorator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveTreeDecorator.class)
public abstract class BeehiveTreeDecoratorMixin extends TreeDecorator {
    @Mutable
    @Shadow @Final private float chance;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inject(float f, CallbackInfo ci) {
        this.chance = f * 2;
    }
}
