package drAAft.mixin;

import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.gen.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(WanderingTraderManager.class)
public abstract class WanderingTraderManagerMixin implements Spawner {
    @Redirect(method = "method_18018", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int injected(Random instance, int i) {
        return 0;
    }
}
