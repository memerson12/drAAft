package draaft.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static draaft.draaft.getDraaftVersion;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin extends DrawableHelper {
    @Shadow protected abstract List<String> getRightText();

    @Redirect(method = "renderRightText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getRightText()Ljava/util/List;"))
    private List<String> redirect(DebugHud instance) {
        List<String> list = this.getRightText();
        list.add("");
        list.add("drAAft v" + getDraaftVersion());

        return list;
    }
}
