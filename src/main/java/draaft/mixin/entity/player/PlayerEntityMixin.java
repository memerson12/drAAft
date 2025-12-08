package draaft.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import draaft.persistent.WorldManifest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "handleFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;handleFallDamage(FF)Z"), index = 0)
    float multiplyFallDamage(float fallDistance) {
        if (!world.isClient() && WorldManifest.get((ServerWorld) world).on(WorldManifest.Feature.DANGEROUS_PEARLS))
        {
            return fallDistance + ((fallDistance - 3.f) * 3f);
        }
        return fallDistance;
    }
}
