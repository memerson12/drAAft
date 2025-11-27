package draaft.mixin.entity;

import draaft.draaft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean canHaveStatusEffect(StatusEffectInstance effect);

    @Shadow
    @Final
    private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow
    protected abstract void onStatusEffectApplied(StatusEffectInstance effect);

    @Shadow
    protected abstract void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getStatusEffects", at = @At("HEAD"))
    void getStatusEffects(CallbackInfoReturnable<Collection<StatusEffectInstance>> cir) {
        if (draaft.ALL_ENTITIES_SPEED) {
            StatusEffectInstance speed = new StatusEffectInstance(StatusEffects.SPEED, 72000, 1, false, false);
            if (this.canHaveStatusEffect(speed)) {
                StatusEffectInstance instance = this.activeStatusEffects.get(speed.getEffectType());
                if (instance == null) {
                    this.activeStatusEffects.put(speed.getEffectType(), speed);
                    this.onStatusEffectApplied(speed);
                } else if (instance.upgrade(speed)) {
                    this.onStatusEffectUpgraded(instance, true);
                }
            }
        }
    }
}
