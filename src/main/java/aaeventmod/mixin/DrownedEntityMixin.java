package aaeventmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin extends ZombieEntity implements RangedAttackMob {
    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public DrownedEntityMixin(World world) {
        super(world);
    }

    @ModifyConstant(method = "initEquipment", constant = @Constant(doubleValue = 0.9))
    private double injected(double value) {
        return 0.7;
    }

    @ModifyConstant(method = "initialize", constant = @Constant(floatValue = 0.03F))
    private float injected(float value) {
        return 0.08F;
    }
}
