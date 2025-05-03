package draaft.mixin.client.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import static draaft.draaft.getDraaftVersion;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin extends DrawableHelper {
    @ModifyReturnValue(method = "getRightText", at = @At("RETURN"))
    private List<String> modifyRightText(List<String> original) {
        original.add("drAAft v" + getDraaftVersion());
        return original;
    }
}
