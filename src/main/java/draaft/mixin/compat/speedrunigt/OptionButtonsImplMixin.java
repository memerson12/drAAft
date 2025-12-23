package draaft.mixin.compat.speedrunigt;

import com.redlimerl.speedrunigt.impl.OptionButtonsImpl;
import draaft.compat.speedrunigt.DraaftCategory;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(OptionButtonsImpl.class)
public abstract class OptionButtonsImplMixin {
    @ModifyArg(
        method = "lambda$createOptionButtons$44",
        at = @At(
            value = "INVOKE",
            target = "Lcom/redlimerl/speedrunigt/api/OptionButtonFactory$Builder;setButtonWidget(Lnet/minecraft/client/gui/widget/AbstractButtonWidget;)Lcom/redlimerl/speedrunigt/api/OptionButtonFactory$Builder;"
        )
    )
    private static AbstractButtonWidget disableLegacyIGTButton(AbstractButtonWidget buttonWidget) {
        if (DraaftCategory.isEnabled()) {
            buttonWidget.active = false;
        }

        return buttonWidget;
    }

    @ModifyArg(
        method = "lambda$createOptionButtons$44",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/redlimerl/speedrunigt/api/OptionButtonFactory$Builder;setToolTip(Ljava/util/function/Supplier;)Lcom/redlimerl/speedrunigt/api/OptionButtonFactory$Builder;"
        )
    )
    private static Supplier<String> injectLegacyIGTTooltip(Supplier<String> toolTipSupplier) {
        return DraaftCategory.isEnabled()
            ? () -> I18n.translate("draaft.compat.speedrunigt.legacyIGTAlwaysOn")
            : toolTipSupplier;
    }
}
