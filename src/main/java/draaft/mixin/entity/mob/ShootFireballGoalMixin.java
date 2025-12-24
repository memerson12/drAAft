package draaft.mixin.entity.mob;

import draaft.persistent.WorldManifest;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GhastEntity.ShootFireballGoal.class)
public abstract class ShootFireballGoalMixin extends Goal {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/GhastEntity;getFireballStrength()I"))
    int getFireballStrength(GhastEntity instance) {
        if (!instance.world.isClient()) {
            if (WorldManifest.get((ServerWorld) instance.world).on(WorldManifest.Feature.SPLODEY_GHASTS)) {
                return 5;
            }
        }
        return 1;
    }
}
