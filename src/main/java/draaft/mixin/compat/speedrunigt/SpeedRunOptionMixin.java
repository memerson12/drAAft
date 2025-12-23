package draaft.mixin.compat.speedrunigt;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.option.OptionArgument;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import draaft.compat.speedrunigt.DraaftCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpeedRunOption.class)
public abstract class SpeedRunOptionMixin {
    @SuppressWarnings("LocalMayUseName")
    @ModifyReturnValue(method = "getOption", at = @At("RETURN"), remap = false)
    private static Object forceLegacyIGT(Object original, @Local(argsOnly = true, ordinal = 0) OptionArgument<?> option) {
        return option.getKey().equals(SpeedRunOptions.TIMER_LEGACY_IGT_MODE.getKey()) && DraaftCategory.isEnabled()
            ? true
            : original;
    }
}
