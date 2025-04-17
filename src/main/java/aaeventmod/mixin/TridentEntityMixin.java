package aaeventmod.mixin;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentEntity.class)
public class TridentEntityMixin {
    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", // Target the onEntityHit method
            at = @At(
                    value = "INVOKE", // Target an invocation instruction
                    target = "Lnet/minecraft/world/World;isThundering()Z" // Specifically target the World.isThundering() method call
            )
    )
    private boolean redirectIsThunderingOnHit(World instance) {
        return true;
    }
}
