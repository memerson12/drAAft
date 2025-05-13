package draaft.mixin.entity.mob;

import draaft.persistent.WorldState;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
    @Redirect(method = "getBarteredItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext$Builder;random(Ljava/util/Random;)Lnet/minecraft/loot/context/LootContext$Builder;"))
    private static LootContext.Builder injected(LootContext.Builder instance, Random random) {
        return instance.random(WorldState.getServerState(instance.getWorld()).getOrCreateRng(WorldState.RngType.BARTER, instance.getWorld()));
    }
}
