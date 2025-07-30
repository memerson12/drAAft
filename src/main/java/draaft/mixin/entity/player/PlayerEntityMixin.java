package draaft.mixin.entity.player;

import draaft.BackgroundTaskRunner;
import draaft.RemoteMapUpdater;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        BackgroundTaskRunner.runAsync(() -> {
            RemoteMapUpdater.updateRemoteMap(self.getX(), self.getY(), self.getZ());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(BackgroundTaskRunner::shutdown));
    }

}
