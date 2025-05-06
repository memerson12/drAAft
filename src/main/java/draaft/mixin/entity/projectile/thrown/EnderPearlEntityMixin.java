package draaft.mixin.entity.projectile.thrown;

import draaft.api.PlayerEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Random;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {
    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, double d, double e, double f, World world) {
        super(entityType, d, e, f, world);
    }

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, LivingEntity livingEntity, World world) {
        super(entityType, livingEntity, world);
    }

    @ModifyConstant(method = "onCollision", constant = @Constant(floatValue = 0.05F))
    private float injected(float constant) {
        return 0.07F;
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float injected(Random instance) {
        Entity entity = this.getOwner();
        if (!(entity instanceof PlayerEntityAccessor)) return instance.nextFloat();
        PlayerEntityAccessor playerAccessor = (PlayerEntityAccessor) entity;
        Random draaftPearlRandom = playerAccessor.draaft$getPearlRandom();

        return draaftPearlRandom.nextFloat();
    }
}