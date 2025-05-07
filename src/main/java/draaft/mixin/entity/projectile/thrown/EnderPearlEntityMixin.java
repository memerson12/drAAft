package draaft.mixin.entity.projectile.thrown;

import draaft.draaft;
import draaft.persistent.WorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Random;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {
    @Unique
    private static final Logger logger = draaft.LOGGER;

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
        if (!(this.world instanceof ServerWorld)) {
            logger.warn("EnderPearlEntityMixin - Not ServerWorld");
            return instance.nextFloat();
        }
        ServerWorld world = (ServerWorld) this.getEntityWorld();
        WorldState state = WorldState.getServerState(world);
        Random draaftPearlRng = state.getOrCreatePearlRng(world);

        return draaftPearlRng.nextFloat();
    }
}