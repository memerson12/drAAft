package draaft.mixin.structure;

import net.minecraft.structure.IglooGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(IglooGenerator.class)
public abstract class IglooGeneratorMixin {
    @Redirect(method = "addPieces", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextDouble()D"))
    private static double injected(Random instance) {
        return 0;
    }
}
