package draaft.mixin.server;

import draaft.persistent.WorldManifest;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "sendWorldInfo", at = @At(value = "TAIL"))
    void afterWorldInfo(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        var manifest = WorldManifest.get(player.getServerWorld());

        player.networkHandler.connection.send(manifest.toClientInfo().buildPacket());
    }
}
