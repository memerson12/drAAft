package draaft.mixin.client.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Force isInSingleplayer() to always return true
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"
        )
    )
    private boolean redirectIsInSingleplayer(MinecraftClient instance) {
        return true;
    }

    // Force getPlayerList().size() <= 1 to always pass
    @Redirect(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collection;size()I"
        )
    )
    private int redirectGetPlayerListSize(Collection instance) {
        // Return a dummy list with just one player to make size() <= 1 true
        return 2;
    }
}
