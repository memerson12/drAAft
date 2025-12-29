package draaft.mixin.server.network;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(
        method = "onClientStatus",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                opcode = Opcodes.PUTFIELD,
                target = "Lnet/minecraft/server/network/ServerPlayerEntity;notInAnyWorld:Z"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/advancement/criterion/ChangedDimensionCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/util/registry/RegistryKey;)V"
            )
        ),
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/PlayerManager;respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Z)Lnet/minecraft/server/network/ServerPlayerEntity;"
        )
    )
    void afterEndExit(ClientStatusC2SPacket packet, CallbackInfo ci) {
        for (var effect : this.player.getStatusEffects()) {
            this.sendPacket(new EntityStatusEffectS2CPacket(this.player.getEntityId(), effect));
        }
    }
}
