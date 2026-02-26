package draaft.mixin.server.network;

import com.mojang.authlib.GameProfile;
import draaft.network.HealthResetS2CPacket;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Unique
    private float lastMaxHealth = -1;

    @Inject(method = "copyFrom", at = @At("TAIL"))
    void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (alive && oldPlayer.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
            this.addStatusEffect(oldPlayer.getStatusEffect(StatusEffects.CONDUIT_POWER));
        }
    }

    @Inject(
        method = "playerTick",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;getHealth()F"
        )
    )
    void preHealthSync(CallbackInfo ci) {
        float maxHealth = this.getMaxHealth();

        if (this.lastMaxHealth != maxHealth) {
            this.lastMaxHealth = maxHealth;

            if (this.getHealth() > maxHealth) {
                this.setHealth(maxHealth);
                this.networkHandler.sendPacket(new HealthResetS2CPacket(maxHealth).buildPacket());
            }
        }
    }
}
