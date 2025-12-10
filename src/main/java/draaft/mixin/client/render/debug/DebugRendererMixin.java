package draaft.mixin.client.render.debug;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasReducedDebugInfo()Z"))
    boolean ignoreReducedDebugInfo(MinecraftClient instance) {
        return false;
    }
}
