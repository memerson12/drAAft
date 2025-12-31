package draaft.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import draaft.mixin.entity.LivingEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
    @Redirect(
        method = "finishUsing",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z")
    )
    boolean keepConduitPower(LivingEntity instance, @Local(argsOnly = true) LivingEntity user) {
        var effectIter = user.getStatusEffects().iterator();

        while (effectIter.hasNext()) {
            var effect = effectIter.next();

            if (effect.getEffectType().equals(StatusEffects.CONDUIT_POWER)) {
                continue;
            }

            ((LivingEntityAccessor) user).draaft$onStatusEffectRemoved(effect);
            effectIter.remove();
        }

        return true;
    }
}
