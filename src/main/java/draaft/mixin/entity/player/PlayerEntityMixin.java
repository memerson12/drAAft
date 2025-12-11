package draaft.mixin.entity.player;

import draaft.client.DraaftState;
import draaft.client.ServerClient;
import draaft.client.ws.outgoing.PositionUpdate;
import draaft.persistent.WorldManifest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Unique
    private static int currentTickCount = 0;
    @Unique
    private static final float UPDATE_FREQUENCY_SECONDS = 0.5f;
    @Unique
    private static final float TOTAL_TICKS_TO_WAIT = UPDATE_FREQUENCY_SECONDS * 20;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "handleFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;handleFallDamage(FF)Z"), index = 0)
    float multiplyFallDamage(float fallDistance) {
        if (!world.isClient() && WorldManifest.get((ServerWorld) world).on(WorldManifest.Feature.DANGEROUS_PEARLS)) {
            return fallDistance + ((fallDistance - 3.0F) * 3.0F);
        }
        return fallDistance;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (!DraaftState.isAccessible() ||
            !DraaftState.getInstance().inDraaftWorld() ||
            !DraaftState.getInstance().isTournament()
        ) return;

        if (currentTickCount >= TOTAL_TICKS_TO_WAIT) {
            currentTickCount = 0;

            String worldId = this.world.getRegistryKey().getValue().toString();

            ServerClient serverClient = ServerClient.getInstanceOrNull();
            if(serverClient != null) {
                serverClient.uploadPosition(
                    new PositionUpdate.PositionUpdateEvent(this.getX(), this.getY(), this.getZ(), worldId)
                );
            }
        } else {
            currentTickCount++;
        }

    }
}
