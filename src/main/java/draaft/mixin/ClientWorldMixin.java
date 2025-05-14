package draaft.mixin;

import draaft.draaft;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Shadow @Final private ClientChunkManager chunkManager;
    @Unique int count = 0;

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        if(count == 20) {
            int numChunks = chunkManager.getLoadedChunkCount();
            draaft.LOGGER.info("Loaded " + numChunks + " chunks");
            count = 0;
        }
        count++;
    }
}